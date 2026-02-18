package com.medico.repository;

import com.medico.entity.HealthAlert;
import com.medico.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthAlertRepository extends JpaRepository<HealthAlert, Long> {
    List<HealthAlert> findByDoctorOrderByCreatedAtDesc(Doctor doctor);

    Page<HealthAlert> findByDoctorIdOrderByCreatedAtDesc(Long doctorId, Pageable pageable);

    List<HealthAlert> findByDoctorIdAndIsReadFalseOrderByCreatedAtDesc(Long doctorId);

    long countByDoctorIdAndIsReadFalse(Long doctorId);

    List<HealthAlert> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}
