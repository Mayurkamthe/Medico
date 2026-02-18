package com.medico.repository;

import com.medico.entity.VitalReading;
import com.medico.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VitalReadingRepository extends JpaRepository<VitalReading, Long> {
    List<VitalReading> findByPatientOrderByRecordedAtDesc(Patient patient);

    Page<VitalReading> findByPatientIdOrderByRecordedAtDesc(Long patientId, Pageable pageable);

    Optional<VitalReading> findFirstByPatientOrderByRecordedAtDesc(Patient patient);

    Optional<VitalReading> findFirstByPatientIdOrderByRecordedAtDesc(Long patientId);

    @Query("SELECT v FROM VitalReading v WHERE v.patient.id = :patientId AND v.recordedAt BETWEEN :start AND :end ORDER BY v.recordedAt DESC")
    List<VitalReading> findByPatientIdAndDateRange(
            @Param("patientId") Long patientId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<VitalReading> findTop10ByPatientIdOrderByRecordedAtDesc(Long patientId);

    // Keep only 5 records per patient
    List<VitalReading> findTop5ByPatientIdOrderByRecordedAtDesc(Long patientId);

    // Count total vitals for a patient
    long countByPatientId(Long patientId);

    // Find vitals to delete (older than the top 5)
    @Query("SELECT v FROM VitalReading v WHERE v.patient.id = :patientId AND v.id NOT IN " +
            "(SELECT v2.id FROM VitalReading v2 WHERE v2.patient.id = :patientId ORDER BY v2.recordedAt DESC LIMIT 5)")
    List<VitalReading> findOldVitalsToDelete(@Param("patientId") Long patientId);

    // Delete vitals by IDs
    @Modifying
    @Query("DELETE FROM VitalReading v WHERE v.id IN :ids")
    void deleteByIds(@Param("ids") List<Long> ids);
}
