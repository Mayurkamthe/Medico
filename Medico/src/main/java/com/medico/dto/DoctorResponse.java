package com.medico.dto;

import com.medico.entity.Doctor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponse {
    private Long id;
    private String email;
    private String fullName;
    private String specialization;
    private String licenseNumber;
    private String phoneNumber;

    public static DoctorResponse fromEntity(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .email(doctor.getEmail())
                .fullName(doctor.getFullName())
                .specialization(doctor.getSpecialization())
                .licenseNumber(doctor.getLicenseNumber())
                .phoneNumber(doctor.getPhoneNumber())
                .build();
    }
}
