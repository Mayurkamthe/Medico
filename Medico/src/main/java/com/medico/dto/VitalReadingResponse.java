package com.medico.dto;

import com.medico.entity.RiskLevel;
import com.medico.entity.VitalReading;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitalReadingResponse {
    private Long id;
    private Long patientId;
    private Integer heartRate;
    private Integer spo2;
    private Double temperature;
    private RiskLevel riskLevel;
    private String deviceId;

    // Clinical Recommendations
    private Integer scenarioId;
    private String specificCause;
    private String possibleCauses;
    private String symptoms;
    private String recommendations;

    private LocalDateTime recordedAt;

    public static VitalReadingResponse fromEntity(VitalReading vitalReading) {
        return VitalReadingResponse.builder()
                .id(vitalReading.getId())
                .patientId(vitalReading.getPatient().getId())
                .heartRate(vitalReading.getHeartRate())
                .spo2(vitalReading.getSpo2())
                .temperature(vitalReading.getTemperature())
                .riskLevel(vitalReading.getRiskLevel())
                .deviceId(vitalReading.getDeviceId())
                .scenarioId(vitalReading.getScenarioId())
                .specificCause(vitalReading.getSpecificCause())
                .possibleCauses(vitalReading.getPossibleCauses())
                .symptoms(vitalReading.getSymptoms())
                .recommendations(vitalReading.getRecommendations())
                .recordedAt(vitalReading.getRecordedAt())
                .build();
    }
}
