package com.medico.service;

import com.medico.dto.*;
import com.medico.entity.*;
import com.medico.repository.PatientDiseaseHistoryRepository;
import com.medico.repository.PatientRepository;
import com.medico.repository.VitalReadingRepository;
import com.medico.security.DoctorPrincipal;
import com.medico.service.DiseaseKnowledgeBase.DiseaseInfo;
import com.medico.service.DiseaseKnowledgeBase.DiseaseMatch;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing patient disease history
 */
@Service
@RequiredArgsConstructor
public class DiseaseHistoryService {

    private final PatientDiseaseHistoryRepository diseaseHistoryRepository;
    private final PatientRepository patientRepository;
    private final VitalReadingRepository vitalReadingRepository;
    private final DiseaseMatchingService diseaseMatchingService;

    private Long getCurrentDoctorId() {
        DoctorPrincipal principal = (DoctorPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.getId();
    }

    /**
     * Get all disease history for a patient
     */
    @Transactional(readOnly = true)
    public List<DiseaseHistoryResponse> getPatientDiseaseHistory(Long patientId) {
        Long doctorId = getCurrentDoctorId();
        patientRepository.findByIdAndAssignedDoctorId(patientId, doctorId)
                .orElseThrow(() -> new RuntimeException("Patient not found or access denied"));

        List<PatientDiseaseHistory> history = diseaseHistoryRepository
                .findByPatientIdOrderByStatusAndDetectedAt(patientId);
        return DiseaseHistoryResponse.fromEntities(history);
    }

    /**
     * Get paginated disease history
     */
    @Transactional(readOnly = true)
    public Page<DiseaseHistoryResponse> getPatientDiseaseHistoryPaginated(Long patientId, int page, int size) {
        Long doctorId = getCurrentDoctorId();
        patientRepository.findByIdAndAssignedDoctorId(patientId, doctorId)
                .orElseThrow(() -> new RuntimeException("Patient not found or access denied"));

        Page<PatientDiseaseHistory> historyPage = diseaseHistoryRepository
                .findByPatientIdOrderByDetectedAtDesc(patientId, PageRequest.of(page, size));
        return historyPage.map(DiseaseHistoryResponse::fromEntity);
    }

    /**
     * Get active diseases for a patient
     */
    @Transactional(readOnly = true)
    public List<DiseaseHistoryResponse> getActiveDiseases(Long patientId) {
        Long doctorId = getCurrentDoctorId();
        patientRepository.findByIdAndAssignedDoctorId(patientId, doctorId)
                .orElseThrow(() -> new RuntimeException("Patient not found or access denied"));

        List<PatientDiseaseHistory> active = diseaseHistoryRepository
                .findByPatientIdAndStatusOrderByDetectedAtDesc(patientId, DiseaseStatus.ACTIVE);
        return DiseaseHistoryResponse.fromEntities(active);
    }

    /**
     * Get disease history summary
     */
    @Transactional(readOnly = true)
    public DiseaseHistorySummary getPatientDiseaseSummary(Long patientId) {
        Long doctorId = getCurrentDoctorId();
        patientRepository.findByIdAndAssignedDoctorId(patientId, doctorId)
                .orElseThrow(() -> new RuntimeException("Patient not found or access denied"));

        long activeCount = diseaseHistoryRepository.countByPatientIdAndStatus(patientId, DiseaseStatus.ACTIVE);
        long monitoringCount = diseaseHistoryRepository.countByPatientIdAndStatus(patientId, DiseaseStatus.MONITORING);
        long clearedCount = diseaseHistoryRepository.countByPatientIdAndStatus(patientId, DiseaseStatus.CLEARED);
        long chronicCount = diseaseHistoryRepository.countByPatientIdAndStatus(patientId, DiseaseStatus.CHRONIC);

        List<DiseaseHistoryResponse> active = getActiveDiseases(patientId);

        return new DiseaseHistorySummary(activeCount, monitoringCount, clearedCount, chronicCount, active);
    }

    /**
     * Record a disease to patient history (from detected match)
     */
    @Transactional
    public DiseaseHistoryResponse recordDiseaseFromMatch(Long patientId, DiseaseMatch match) {
        Long doctorId = getCurrentDoctorId();
        Patient patient = patientRepository.findByIdAndAssignedDoctorId(patientId, doctorId)
                .orElseThrow(() -> new RuntimeException("Patient not found or access denied"));

        // Check if this disease is already active for the patient
        Optional<PatientDiseaseHistory> existing = diseaseHistoryRepository
                .findByPatientIdAndDiseaseIdAndStatus(patientId, match.getDisease().getId(), DiseaseStatus.ACTIVE);

        if (existing.isPresent()) {
            // Update existing record
            PatientDiseaseHistory history = existing.get();
            history.setDetectionConfidence(match.getConfidence());
            return DiseaseHistoryResponse.fromEntity(diseaseHistoryRepository.save(history));
        }

        // Get latest vitals
        VitalReading latestVitals = vitalReadingRepository.findFirstByPatientIdOrderByRecordedAtDesc(patientId)
                .orElse(null);

        // Create new record
        PatientDiseaseHistory history = PatientDiseaseHistory.builder()
                .patient(patient)
                .diseaseId(match.getDisease().getId())
                .diseaseName(match.getDisease().getName())
                .possibleCauses(match.getDisease().getPossibleCauses())
                .status(DiseaseStatus.ACTIVE)
                .detectionConfidence(match.getConfidence())
                .detectedTemperature(latestVitals != null ? latestVitals.getTemperature() : null)
                .detectedHeartRate(latestVitals != null ? latestVitals.getHeartRate() : null)
                .detectedSpo2(latestVitals != null ? latestVitals.getSpo2() : null)
                .observedSymptoms(String.join(", ", match.getDisease().getSymptoms()))
                .build();

        return DiseaseHistoryResponse.fromEntity(diseaseHistoryRepository.save(history));
    }

    /**
     * Record a disease manually
     */
    @Transactional
    public DiseaseHistoryResponse recordDiseaseManually(Long patientId, RecordDiseaseRequest request) {
        Long doctorId = getCurrentDoctorId();
        Patient patient = patientRepository.findByIdAndAssignedDoctorId(patientId, doctorId)
                .orElseThrow(() -> new RuntimeException("Patient not found or access denied"));

        PatientDiseaseHistory history = PatientDiseaseHistory.builder()
                .patient(patient)
                .diseaseId(request.getDiseaseId())
                .diseaseName(request.getDiseaseName())
                .possibleCauses(request.getPossibleCauses())
                .status(DiseaseStatus.ACTIVE)
                .detectionConfidence(request.getDetectionConfidence())
                .detectedTemperature(request.getDetectedTemperature())
                .detectedHeartRate(request.getDetectedHeartRate())
                .detectedSpo2(request.getDetectedSpo2())
                .observedSymptoms(request.getObservedSymptoms())
                .doctorNotes(request.getDoctorNotes())
                .build();

        return DiseaseHistoryResponse.fromEntity(diseaseHistoryRepository.save(history));
    }

    /**
     * Clear/resolve a disease
     */
    @Transactional
    public DiseaseHistoryResponse clearDisease(Long historyId, ClearDiseaseRequest request) {
        Long doctorId = getCurrentDoctorId();

        PatientDiseaseHistory history = diseaseHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("Disease history record not found"));

        // Verify access
        if (!history.getPatient().getAssignedDoctor().getId().equals(doctorId)) {
            throw new RuntimeException("Access denied");
        }

        history.setStatus(DiseaseStatus.CLEARED);
        history.setClearedAt(LocalDateTime.now());
        history.setClearedByDoctorId(doctorId);
        history.setClearanceNotes(request.getClearanceNotes());

        return DiseaseHistoryResponse.fromEntity(diseaseHistoryRepository.save(history));
    }

    /**
     * Update disease status
     */
    @Transactional
    public DiseaseHistoryResponse updateDiseaseStatus(Long historyId, DiseaseStatus newStatus, String notes) {
        Long doctorId = getCurrentDoctorId();

        PatientDiseaseHistory history = diseaseHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("Disease history record not found"));

        // Verify access
        if (!history.getPatient().getAssignedDoctor().getId().equals(doctorId)) {
            throw new RuntimeException("Access denied");
        }

        history.setStatus(newStatus);
        if (newStatus == DiseaseStatus.CLEARED) {
            history.setClearedAt(LocalDateTime.now());
            history.setClearedByDoctorId(doctorId);
        }
        if (notes != null) {
            history.setDoctorNotes(notes);
        }

        return DiseaseHistoryResponse.fromEntity(diseaseHistoryRepository.save(history));
    }

    /**
     * Add notes to disease record
     */
    @Transactional
    public DiseaseHistoryResponse addNotes(Long historyId, String notes) {
        Long doctorId = getCurrentDoctorId();

        PatientDiseaseHistory history = diseaseHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("Disease history record not found"));

        if (!history.getPatient().getAssignedDoctor().getId().equals(doctorId)) {
            throw new RuntimeException("Access denied");
        }

        String existingNotes = history.getDoctorNotes() != null ? history.getDoctorNotes() + "\n" : "";
        history.setDoctorNotes(existingNotes + "[" + LocalDateTime.now().toString() + "] " + notes);

        return DiseaseHistoryResponse.fromEntity(diseaseHistoryRepository.save(history));
    }

    /**
     * Auto-record diseases from current matches
     */
    @Transactional
    public List<DiseaseHistoryResponse> autoRecordFromMatches(Long patientId) {
        List<DiseaseMatch> matches = diseaseMatchingService.matchDiseasesForPatient(patientId);

        return matches.stream()
                .filter(m -> m.getConfidence() >= 60) // Only record if confidence >= 60%
                .map(m -> recordDiseaseFromMatch(patientId, m))
                .collect(Collectors.toList());
    }

    /**
     * Get all available diseases from knowledge base
     */
    public List<DiseaseInfo> getAllDiseases() {
        return DiseaseKnowledgeBase.getAllDiseases();
    }

    /**
     * Summary DTO
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class DiseaseHistorySummary {
        private long activeCount;
        private long monitoringCount;
        private long clearedCount;
        private long chronicCount;
        private List<DiseaseHistoryResponse> activeDiseases;
    }
}
