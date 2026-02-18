package com.medico.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for recording a disease to patient history
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordDiseaseRequest {
    private Integer diseaseId;
    private String diseaseName;
    private String possibleCauses;
    private Double detectionConfidence;
    private Double detectedTemperature;
    private Integer detectedHeartRate;
    private Integer detectedSpo2;
    private String observedSymptoms;
    private String doctorNotes;
}
