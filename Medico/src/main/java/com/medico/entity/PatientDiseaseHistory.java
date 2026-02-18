package com.medico.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity to track disease history per patient.
 * Records detected diseases, their status (active/cleared), and timeline.
 */
@Entity
@Table(name = "patient_disease_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDiseaseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private Integer diseaseId; // References DiseaseKnowledgeBase disease ID

    @Column(nullable = false)
    private String diseaseName;

    @Column(length = 500)
    private String possibleCauses;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DiseaseStatus status = DiseaseStatus.ACTIVE;

    // Confidence when disease was detected
    private Double detectionConfidence;

    // Vitals at time of detection
    private Double detectedTemperature;
    private Integer detectedHeartRate;
    private Integer detectedSpo2;

    // Symptoms reported/observed
    @Column(length = 1000)
    private String observedSymptoms;

    // Notes from doctor
    @Column(length = 2000)
    private String doctorNotes;

    // Timeline tracking
    @Column(nullable = false, updatable = false)
    private LocalDateTime detectedAt;

    private LocalDateTime clearedAt;

    private LocalDateTime updatedAt;

    // Who marked it as cleared (doctor ID)
    private Long clearedByDoctorId;

    // Clearance reason/notes
    @Column(length = 1000)
    private String clearanceNotes;

    @PrePersist
    protected void onCreate() {
        detectedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = DiseaseStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
