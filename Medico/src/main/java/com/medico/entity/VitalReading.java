package com.medico.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vital_readings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitalReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private Integer heartRate;

    @Column(nullable = false)
    private Integer spo2;

    @Column(nullable = false)
    private Double temperature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    private String deviceId;

    // Clinical Recommendations Fields
    private Integer scenarioId; // 1-16 based on vital combinations

    @Column(length = 500)
    private String specificCause; // e.g., "Bradycardia, Hypothermia"

    @Column(length = 500)
    private String possibleCauses; // e.g., "Cardiac issues, hypothermia"

    @Column(length = 500)
    private String symptoms; // e.g., "Dizziness, fainting, chest pain"

    @Column(length = 2000)
    private String recommendations; // Detailed recommendations

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}
