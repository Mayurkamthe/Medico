package com.medico.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String patientCode;

    @Column(nullable = false)
    private String fullName;

    private Integer age;

    private String gender;

    private String bloodGroup;

    private String phoneNumber;

    private String address;

    private String medicalHistory;

    // IoT Device ID mapping
    @Column(unique = true)
    private String deviceId; // Current or last assigned device

    // Device Assignment Tracking
    private LocalDateTime deviceAssignedAt; // When device was assigned (null = not assigned)
    private Integer deviceAssignmentDuration; // Optional: duration in seconds for auto-expiry

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor assignedDoctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_risk_level", nullable = false)
    private RiskLevel currentRiskLevel = RiskLevel.NORMAL; // Default to NORMAL for new patients

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VitalReading> vitalReadings;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HealthAlert> healthAlerts;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AIAnalysisResult> aiAnalysisResults;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (patientCode == null) {
            patientCode = "PAT-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
