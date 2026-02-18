package com.medico.service;

import com.medico.dto.IoTVitalRequest;
import com.medico.dto.VitalReadingResponse;
import com.medico.entity.*;
import com.medico.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for processing vital readings from IoT devices.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VitalService {

    private final VitalReadingRepository vitalReadingRepository;
    private final PatientRepository patientRepository;
    private final PatientService patientService; // Added for device assignment lookup
    private final HealthAlertRepository healthAlertRepository;
    private final RiskPredictionService riskPredictionService;
    private final GeminiAIService geminiAIService;
    private final NotificationService notificationService;

    /**
     * Maximum number of vital records to keep per patient
     */
    private static final int MAX_VITALS_PER_PATIENT = 5;

    /**
     * Process incoming vital data from IoT device.
     * This is the main entry point for IoT data ingestion.
     */
    @Transactional
    public VitalReadingResponse processIoTVitals(IoTVitalRequest request) {
        // Find patient WITH ACTIVE device assignment
        Patient patient = patientService.findActivePatientForDevice(request.getDeviceId())
                .orElseThrow(() -> new RuntimeException(
                        "No active patient assignment for device ID: " + request.getDeviceId()));

        // Match clinical scenario and get recommendations
        ClinicalScenario scenario = riskPredictionService.matchClinicalScenario(
                request.getHeartRate(),
                request.getSpo2(),
                request.getTemperature());

        RiskLevel riskLevel = scenario.getRiskLevel();

        // Create and save vital reading with clinical recommendations
        VitalReading vitalReading = VitalReading.builder()
                .patient(patient)
                .heartRate(request.getHeartRate())
                .spo2(request.getSpo2())
                .temperature(request.getTemperature())
                .riskLevel(riskLevel)
                .deviceId(request.getDeviceId())
                // Clinical scenario fields
                .scenarioId(scenario.getScenarioId())
                .specificCause(scenario.getSpecificCause())
                .possibleCauses(scenario.getPossibleCauses())
                .symptoms(scenario.getSymptoms())
                .recommendations(scenario.getRecommendations())
                .recordedAt(LocalDateTime.now())
                .build();

        vitalReading = vitalReadingRepository.save(vitalReading);

        // Update patient's current risk level
        patient.setCurrentRiskLevel(riskLevel);
        patientRepository.save(patient);

        // Cleanup old records - keep only 5 most recent
        cleanupOldVitalRecords(patient.getId());

        // Handle alerts based on risk level
        handleRiskAlerts(patient, vitalReading, riskLevel);

        log.info("Processed vitals for patient {} - Scenario: {}, Risk: {}",
                patient.getId(), scenario.getScenarioId(), riskLevel);

        return VitalReadingResponse.fromEntity(vitalReading);
    }

    /**
     * Delete old vital records, keeping only the 5 most recent per patient.
     */
    private void cleanupOldVitalRecords(Long patientId) {
        long totalCount = vitalReadingRepository.countByPatientId(patientId);

        if (totalCount > MAX_VITALS_PER_PATIENT) {
            List<VitalReading> oldVitals = vitalReadingRepository.findOldVitalsToDelete(patientId);

            if (!oldVitals.isEmpty()) {
                List<Long> idsToDelete = oldVitals.stream()
                        .map(VitalReading::getId)
                        .collect(Collectors.toList());

                vitalReadingRepository.deleteByIds(idsToDelete);
                log.info("Deleted {} old vital records for patient {}, keeping {} most recent",
                        idsToDelete.size(), patientId, MAX_VITALS_PER_PATIENT);
            }
        }
    }

    /**
     * Handle alerts and AI analysis based on risk level.
     */
    private void handleRiskAlerts(Patient patient, VitalReading vitalReading, RiskLevel riskLevel) {
        Doctor doctor = patient.getAssignedDoctor();
        String riskSummary = riskPredictionService.getRiskSummary(
                vitalReading.getHeartRate(),
                vitalReading.getSpo2(),
                vitalReading.getTemperature());

        if (riskLevel == RiskLevel.CRITICAL) {
            // Create critical alert
            HealthAlert alert = HealthAlert.builder()
                    .patient(patient)
                    .doctor(doctor)
                    .alertType(AlertType.CRITICAL)
                    .message(riskSummary)
                    .vitalReading(vitalReading)
                    .isRead(false)
                    .build();
            healthAlertRepository.save(alert);

            // Send push notification
            notificationService.sendCriticalAlert(doctor, patient, riskSummary);

            // AI analysis is now manual - doctor must click "Analyze" button to trigger
            log.info("Critical vitals detected for patient {} - AI analysis available on-demand", patient.getId());

        } else if (riskLevel == RiskLevel.MODERATE) {
            // Create warning alert
            HealthAlert alert = HealthAlert.builder()
                    .patient(patient)
                    .doctor(doctor)
                    .alertType(AlertType.WARNING)
                    .message(riskSummary)
                    .vitalReading(vitalReading)
                    .isRead(false)
                    .build();
            healthAlertRepository.save(alert);

            // Send warning notification
            notificationService.sendWarningAlert(doctor, patient, riskSummary);
        }
    }

    /**
     * Get vital history for a patient.
     */
    @Transactional(readOnly = true)
    public Page<VitalReadingResponse> getVitalHistory(Long patientId, Pageable pageable) {
        Page<VitalReading> vitals = vitalReadingRepository.findByPatientIdOrderByRecordedAtDesc(patientId, pageable);
        return vitals.map(VitalReadingResponse::fromEntity);
    }

    /**
     * Get latest vital reading for a patient.
     */
    @Transactional(readOnly = true)
    public VitalReadingResponse getLatestVitals(Long patientId) {
        VitalReading vitalReading = vitalReadingRepository.findFirstByPatientIdOrderByRecordedAtDesc(patientId)
                .orElseThrow(() -> new RuntimeException("No vital readings found for patient"));
        return VitalReadingResponse.fromEntity(vitalReading);
    }

    /**
     * Get recent vitals (last 5) for a patient.
     * Note: Backend only keeps 5 records max per patient.
     */
    @Transactional(readOnly = true)
    public List<VitalReadingResponse> getRecentVitals(Long patientId) {
        List<VitalReading> vitals = vitalReadingRepository.findTop5ByPatientIdOrderByRecordedAtDesc(patientId);
        return vitals.stream()
                .map(VitalReadingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get vitals within a date range.
     */
    @Transactional(readOnly = true)
    public List<VitalReadingResponse> getVitalsByDateRange(Long patientId, LocalDateTime start, LocalDateTime end) {
        List<VitalReading> vitals = vitalReadingRepository.findByPatientIdAndDateRange(patientId, start, end);
        return vitals.stream()
                .map(VitalReadingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Check if a device is currently monitoring an active patient.
     */
    @Transactional(readOnly = true)
    public boolean isDeviceActive(String deviceId) {
        return patientService.findActivePatientForDevice(deviceId).isPresent();
    }
}
