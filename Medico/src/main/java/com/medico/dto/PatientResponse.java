package com.medico.dto;

import com.medico.entity.Patient;
import com.medico.entity.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponse {
    private Long id;
    private String patientCode;
    private String fullName;
    private Integer age;
    private String gender;
    private String bloodGroup;
    private String phoneNumber;
    private String address;
    private String medicalHistory;
    private String deviceId;
    private LocalDateTime deviceAssignedAt;
    private Integer deviceAssignmentDuration;
    private RiskLevel currentRiskLevel;
    private LocalDateTime createdAt;
    private VitalReadingResponse latestVitals;

    public static PatientResponse fromEntity(Patient patient) {
        return PatientResponse.builder()
                .id(patient.getId())
                .patientCode(patient.getPatientCode())
                .fullName(patient.getFullName())
                .age(patient.getAge())
                .gender(patient.getGender())
                .bloodGroup(patient.getBloodGroup())
                .phoneNumber(patient.getPhoneNumber())
                .address(patient.getAddress())
                .medicalHistory(patient.getMedicalHistory())
                .deviceId(patient.getDeviceId())
                .deviceAssignedAt(patient.getDeviceAssignedAt())
                .deviceAssignmentDuration(patient.getDeviceAssignmentDuration())
                .currentRiskLevel(patient.getCurrentRiskLevel())
                .createdAt(patient.getCreatedAt())
                .build();
    }
}
