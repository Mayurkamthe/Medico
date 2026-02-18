package com.medico.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private DoctorResponse doctor;

    public AuthResponse(String token, DoctorResponse doctor) {
        this.token = token;
        this.tokenType = "Bearer";
        this.doctor = doctor;
    }
}
