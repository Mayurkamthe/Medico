package com.medico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IoTVitalRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotNull(message = "Heart rate is required")
    private Integer heartRate;

    @NotNull(message = "SpO2 is required")
    private Integer spo2;

    @NotNull(message = "Temperature is required")
    private Double temperature;
}
