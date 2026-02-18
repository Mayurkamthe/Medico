package com.medico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Age is required")
    private Integer age;

    private String gender;

    private String bloodGroup;

    private String phoneNumber;

    private String address;

    private String medicalHistory;

    private String deviceId;
}
