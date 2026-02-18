package com.medico.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceAssignRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    // Optional: auto-expire assignment after X seconds (e.g., 3600 = 1 hour)
    private Integer durationSeconds;
}
