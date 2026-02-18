package com.medico.service;

import com.medico.dto.HealthAlertResponse;
import com.medico.entity.HealthAlert;
import com.medico.repository.HealthAlertRepository;
import com.medico.security.DoctorPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final HealthAlertRepository healthAlertRepository;

    private Long getCurrentDoctorId() {
        DoctorPrincipal principal = (DoctorPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.getId();
    }

    @Transactional(readOnly = true)
    public Page<HealthAlertResponse> getAlerts(Pageable pageable) {
        Long doctorId = getCurrentDoctorId();
        Page<HealthAlert> alerts = healthAlertRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId, pageable);
        return alerts.map(HealthAlertResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<HealthAlertResponse> getUnreadAlerts() {
        Long doctorId = getCurrentDoctorId();
        List<HealthAlert> alerts = healthAlertRepository.findByDoctorIdAndIsReadFalseOrderByCreatedAtDesc(doctorId);
        return alerts.stream()
                .map(HealthAlertResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        Long doctorId = getCurrentDoctorId();
        return healthAlertRepository.countByDoctorIdAndIsReadFalse(doctorId);
    }

    @Transactional
    public void markAsRead(Long alertId) {
        Long doctorId = getCurrentDoctorId();
        HealthAlert alert = healthAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (!alert.getDoctor().getId().equals(doctorId)) {
            throw new RuntimeException("Unauthorized access to alert");
        }

        alert.setIsRead(true);
        healthAlertRepository.save(alert);
    }

    @Transactional
    public void markAllAsRead() {
        Long doctorId = getCurrentDoctorId();
        List<HealthAlert> unreadAlerts = healthAlertRepository
                .findByDoctorIdAndIsReadFalseOrderByCreatedAtDesc(doctorId);
        unreadAlerts.forEach(alert -> alert.setIsRead(true));
        healthAlertRepository.saveAll(unreadAlerts);
    }

    @Transactional(readOnly = true)
    public List<HealthAlertResponse> getPatientAlerts(Long patientId) {
        Long doctorId = getCurrentDoctorId();
        // Fetch alerts only for this doctor's patients
        List<HealthAlert> alerts = healthAlertRepository.findByPatientIdOrderByCreatedAtDesc(patientId);

        // Filter to ensure patient belongs to this doctor (security check)
        return alerts.stream()
                .filter(alert -> alert.getDoctor().getId().equals(doctorId))
                .map(HealthAlertResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
