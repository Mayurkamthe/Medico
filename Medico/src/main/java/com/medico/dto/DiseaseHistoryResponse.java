package com.medico.dto;

import com.medico.entity.DiseaseStatus;
import com.medico.entity.PatientDiseaseHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for patient disease history
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiseaseHistoryResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Integer diseaseId;
    private String diseaseName;
    private String possibleCauses;
    private String status;
    private Double detectionConfidence;
    private Double detectedTemperature;
    private Integer detectedHeartRate;
    private Integer detectedSpo2;
    private String observedSymptoms;
    private String doctorNotes;
    private LocalDateTime detectedAt;
    private LocalDateTime clearedAt;
    private String clearanceNotes;
    private Long durationDays; // Days since detected or until cleared

    public static DiseaseHistoryResponse fromEntity(PatientDiseaseHistory entity) {
        DiseaseHistoryResponse response = new DiseaseHistoryResponse();
        response.setId(entity.getId());
        response.setPatientId(entity.getPatient().getId());
        response.setPatientName(entity.getPatient().getFullName());
        response.setDiseaseId(entity.getDiseaseId());
        response.setDiseaseName(entity.getDiseaseName());
        response.setPossibleCauses(entity.getPossibleCauses());
        response.setStatus(entity.getStatus().name());
        response.setDetectionConfidence(entity.getDetectionConfidence());
        response.setDetectedTemperature(entity.getDetectedTemperature());
        response.setDetectedHeartRate(entity.getDetectedHeartRate());
        response.setDetectedSpo2(entity.getDetectedSpo2());
        response.setObservedSymptoms(entity.getObservedSymptoms());
        response.setDoctorNotes(entity.getDoctorNotes());
        response.setDetectedAt(entity.getDetectedAt());
        response.setClearedAt(entity.getClearedAt());
        response.setClearanceNotes(entity.getClearanceNotes());

        // Calculate duration
        LocalDateTime endDate = entity.getClearedAt() != null ? entity.getClearedAt() : LocalDateTime.now();
        response.setDurationDays(java.time.temporal.ChronoUnit.DAYS.between(entity.getDetectedAt(), endDate));

        return response;
    }

    public static List<DiseaseHistoryResponse> fromEntities(List<PatientDiseaseHistory> entities) {
        return entities.stream()
                .map(DiseaseHistoryResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
