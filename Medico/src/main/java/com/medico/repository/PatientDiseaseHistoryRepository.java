package com.medico.repository;

import com.medico.entity.DiseaseStatus;
import com.medico.entity.PatientDiseaseHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientDiseaseHistoryRepository extends JpaRepository<PatientDiseaseHistory, Long> {

    // Find all disease history for a patient, ordered by detection date
    List<PatientDiseaseHistory> findByPatientIdOrderByDetectedAtDesc(Long patientId);

    // Paginated version
    Page<PatientDiseaseHistory> findByPatientIdOrderByDetectedAtDesc(Long patientId, Pageable pageable);

    // Find active diseases for a patient
    List<PatientDiseaseHistory> findByPatientIdAndStatusOrderByDetectedAtDesc(Long patientId, DiseaseStatus status);

    // Find by patient and disease ID
    Optional<PatientDiseaseHistory> findByPatientIdAndDiseaseIdAndStatus(Long patientId, Integer diseaseId,
            DiseaseStatus status);

    // Check if patient has had a specific disease
    boolean existsByPatientIdAndDiseaseId(Long patientId, Integer diseaseId);

    // Count active diseases for a patient
    long countByPatientIdAndStatus(Long patientId, DiseaseStatus status);

    // Get all diseases for a patient by status
    @Query("SELECT pdh FROM PatientDiseaseHistory pdh WHERE pdh.patient.id = :patientId " +
            "ORDER BY CASE WHEN pdh.status = 'ACTIVE' THEN 0 WHEN pdh.status = 'MONITORING' THEN 1 ELSE 2 END, pdh.detectedAt DESC")
    List<PatientDiseaseHistory> findByPatientIdOrderByStatusAndDetectedAt(Long patientId);

    // Find recent disease history across all patients for a doctor
    @Query("SELECT pdh FROM PatientDiseaseHistory pdh WHERE pdh.patient.assignedDoctor.id = :doctorId " +
            "ORDER BY pdh.detectedAt DESC")
    Page<PatientDiseaseHistory> findByDoctorIdOrderByDetectedAtDesc(Long doctorId, Pageable pageable);
}
