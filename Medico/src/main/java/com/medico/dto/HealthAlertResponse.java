package com.medico.dto;

import com.medico.entity.AlertType;
import com.medico.entity.HealthAlert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthAlertResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private AlertType alertType;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static HealthAlertResponse fromEntity(HealthAlert alert) {
        return HealthAlertResponse.builder()
                .id(alert.getId())
                .patientId(alert.getPatient().getId())
                .patientName(alert.getPatient().getFullName())
                .alertType(alert.getAlertType())
                .message(alert.getMessage())
                .isRead(alert.getIsRead())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
