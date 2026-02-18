package com.medico.repository;

import com.medico.entity.Patient;
import com.medico.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByAssignedDoctor(Doctor doctor);

    List<Patient> findByAssignedDoctorId(Long doctorId);

    Optional<Patient> findByDeviceId(String deviceId);

    Optional<Patient> findByPatientCode(String patientCode);

    boolean existsByDeviceId(String deviceId);

    boolean existsByPatientCode(String patientCode);

    // Find patients with active device assignments (most recent first)
    @Query("SELECT p FROM Patient p WHERE p.deviceId = :deviceId " +
            "AND p.deviceAssignedAt IS NOT NULL " +
            "ORDER BY p.deviceAssignedAt DESC")
    List<Patient> findRecentlyAssignedByDeviceId(@Param("deviceId") String deviceId);

    Optional<Patient> findByIdAndAssignedDoctorId(Long id, Long doctorId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Patient p SET p.deviceId = NULL, p.deviceAssignedAt = NULL, p.deviceAssignmentDuration = NULL WHERE p.deviceId = :deviceId AND p.id != :currentPatientId")
    void unassignDeviceFromOthers(@Param("deviceId") String deviceId, @Param("currentPatientId") Long currentPatientId);
}
