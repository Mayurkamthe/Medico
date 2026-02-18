package com.medico.service;

import com.medico.dto.PatientRequest;
import com.medico.dto.PatientResponse;
import com.medico.dto.VitalReadingResponse;
import com.medico.entity.Doctor;
import com.medico.entity.Patient;
import com.medico.entity.RiskLevel;
import com.medico.entity.VitalReading;
import com.medico.repository.DoctorRepository;
import com.medico.repository.PatientRepository;
import com.medico.repository.VitalReadingRepository;
import com.medico.security.DoctorPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final VitalReadingRepository vitalReadingRepository;

    private Doctor getCurrentDoctor() {
        DoctorPrincipal principal = (DoctorPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return doctorRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients() {
        Doctor doctor = getCurrentDoctor();
        List<Patient> patients = patientRepository.findByAssignedDoctor(doctor);

        return patients.stream()
                .map(patient -> {
                    PatientResponse response = PatientResponse.fromEntity(patient);
                    // Attach latest vitals
                    Optional<VitalReading> latestVitals = vitalReadingRepository
                            .findFirstByPatientIdOrderByRecordedAtDesc(patient.getId());
                    latestVitals.ifPresent(v -> response.setLatestVitals(VitalReadingResponse.fromEntity(v)));
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        Doctor doctor = getCurrentDoctor();
        Patient patient = patientRepository.findByIdAndAssignedDoctorId(id, doctor.getId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        PatientResponse response = PatientResponse.fromEntity(patient);

        // Attach latest vitals
        Optional<VitalReading> latestVitals = vitalReadingRepository
                .findFirstByPatientIdOrderByRecordedAtDesc(patient.getId());
        latestVitals.ifPresent(v -> response.setLatestVitals(VitalReadingResponse.fromEntity(v)));

        return response;
    }

    @Transactional
    public PatientResponse createPatient(PatientRequest request) {
        DoctorPrincipal currentDoctor = (DoctorPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Doctor doctor = doctorRepository.findById(currentDoctor.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Check for duplicate device ID if provided
        if (request.getDeviceId() != null && !request.getDeviceId().trim().isEmpty()) {
            if (patientRepository.existsByDeviceId(request.getDeviceId())) {
                throw new RuntimeException("Device ID already assigned to another patient");
            }
        }

        Patient patient = Patient.builder()
                .fullName(request.getFullName())
                .age(request.getAge())
                .gender(request.getGender())
                .bloodGroup(request.getBloodGroup())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .medicalHistory(request.getMedicalHistory())
                .deviceId(request.getDeviceId())
                .assignedDoctor(doctor)
                .currentRiskLevel(RiskLevel.NORMAL) // Explicitly set default risk level
                .build();

        Patient savedPatient = patientRepository.save(patient);
        return PatientResponse.fromEntity(savedPatient);
    }

    @Transactional
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        Doctor doctor = getCurrentDoctor();
        Patient patient = patientRepository.findByIdAndAssignedDoctorId(id, doctor.getId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Check if device ID is unique (excluding current patient)
        if (request.getDeviceId() != null && !request.getDeviceId().isEmpty()) {
            Optional<Patient> existingPatient = patientRepository.findByDeviceId(request.getDeviceId());
            if (existingPatient.isPresent() && !existingPatient.get().getId().equals(id)) {
                throw new RuntimeException("Device ID already assigned to another patient");
            }
        }

        patient.setFullName(request.getFullName());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setAddress(request.getAddress());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setDeviceId(request.getDeviceId());

        patient = patientRepository.save(patient);
        return PatientResponse.fromEntity(patient);
    }

    @Transactional
    public void deletePatient(Long id) {
        Doctor doctor = getCurrentDoctor();
        Patient patient = patientRepository.findByIdAndAssignedDoctorId(id, doctor.getId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        patientRepository.deleteById(id);
    }

    // Device Assignment Methods

    @Transactional
    public void assignDevice(Long patientId, String deviceId, Long doctorId) {
        String trimmedDeviceId = deviceId.trim();
        Patient patient = patientRepository.findByIdAndAssignedDoctorId(patientId, doctorId)
                .orElseThrow(() -> new RuntimeException("Patient not found or access denied"));

        // Check if device is already assigned to someone else and force unassign
        patientRepository.unassignDeviceFromOthers(trimmedDeviceId, patientId);
        // Force flush to ensure the update runs before the unique constraint check on
        // the next save
        patientRepository.flush();

        patient.setDeviceId(trimmedDeviceId);
        patient.setDeviceAssignedAt(java.time.LocalDateTime.now());
        patient.setDeviceAssignmentDuration(null); // No expiry by default

        patientRepository.save(patient);
    }

    @Transactional
    public void assignDeviceWithDuration(Long patientId, String deviceId, Integer durationSeconds, Long doctorId) {
        String trimmedDeviceId = deviceId.trim();
        Patient patient = patientRepository.findByIdAndAssignedDoctorId(patientId, doctorId)
                .orElseThrow(() -> new RuntimeException("Patient not found or access denied"));

        // Check if device is already assigned to someone else and force unassign
        patientRepository.unassignDeviceFromOthers(trimmedDeviceId, patientId);
        // Force flush to ensure the update runs before the unique constraint check on
        // the next save
        patientRepository.flush();

        patient.setDeviceId(trimmedDeviceId);
        patient.setDeviceAssignedAt(java.time.LocalDateTime.now());
        patient.setDeviceAssignmentDuration(durationSeconds);

        patientRepository.save(patient);
    }

    @Transactional
    public void unassignDevice(Long patientId, Long doctorId) {
        Patient patient = patientRepository.findByIdAndAssignedDoctorId(patientId, doctorId)
                .orElseThrow(() -> new RuntimeException("Patient not found or access denied"));

        patient.setDeviceAssignedAt(null);
        patient.setDeviceAssignmentDuration(null);
        // Keep deviceId for history

        patientRepository.save(patient);
    }

    @Transactional(readOnly = true)
    public Optional<Patient> findActivePatientForDevice(String deviceId) {
        List<Patient> patients = patientRepository.findRecentlyAssignedByDeviceId(deviceId);

        if (patients.isEmpty()) {
            return Optional.empty();
        }

        Patient mostRecent = patients.get(0);

        // Check if assignment expired
        if (mostRecent.getDeviceAssignmentDuration() != null) {
            java.time.LocalDateTime assignedAt = mostRecent.getDeviceAssignedAt();
            java.time.LocalDateTime expiryTime = assignedAt.plusSeconds(mostRecent.getDeviceAssignmentDuration());

            if (java.time.LocalDateTime.now().isAfter(expiryTime)) {
                return Optional.empty(); // Assignment expired
            }
        }

        return Optional.of(mostRecent);
    }
}
