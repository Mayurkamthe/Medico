package com.medico.service;

import com.medico.entity.Doctor;
import com.medico.entity.HealthAlert;
import com.medico.entity.Patient;
import com.medico.repository.HealthAlertRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for sending push notifications to doctors via Expo Push Notification
 * service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final HealthAlertRepository healthAlertRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${app.expo.push-url}")
    private String expoPushUrl;

    /**
     * Send a critical alert notification to a doctor.
     */
    public void sendCriticalAlert(Doctor doctor, Patient patient, String message) {
        if (doctor.getExpoPushToken() == null || doctor.getExpoPushToken().isEmpty()) {
            log.warn("Doctor {} has no push token registered", doctor.getId());
            return;
        }

        try {
            sendExpoPushNotification(
                    doctor.getExpoPushToken(),
                    "🚨 Critical Alert: " + patient.getFullName(),
                    message,
                    Map.of(
                            "patientId", patient.getId(),
                            "alertType", "CRITICAL",
                            "screen", "PatientDetail"));
            log.info("Critical alert sent to doctor {} for patient {}", doctor.getId(), patient.getId());
        } catch (Exception e) {
            log.error("Failed to send push notification to doctor {}", doctor.getId(), e);
        }
    }

    /**
     * Send a warning notification to a doctor.
     */
    public void sendWarningAlert(Doctor doctor, Patient patient, String message) {
        if (doctor.getExpoPushToken() == null || doctor.getExpoPushToken().isEmpty()) {
            log.warn("Doctor {} has no push token registered", doctor.getId());
            return;
        }

        try {
            sendExpoPushNotification(
                    doctor.getExpoPushToken(),
                    "⚠️ Warning: " + patient.getFullName(),
                    message,
                    Map.of(
                            "patientId", patient.getId(),
                            "alertType", "WARNING",
                            "screen", "PatientDetail"));
            log.info("Warning alert sent to doctor {} for patient {}", doctor.getId(), patient.getId());
        } catch (Exception e) {
            log.error("Failed to send push notification to doctor {}", doctor.getId(), e);
        }
    }

    private void sendExpoPushNotification(String pushToken, String title, String body, Map<String, Object> data) {
        WebClient webClient = webClientBuilder.build();

        Map<String, Object> message = new HashMap<>();
        message.put("to", pushToken);
        message.put("title", title);
        message.put("body", body);
        message.put("data", data);
        message.put("sound", "default");
        message.put("priority", "high");
        message.put("channelId", "medico-alerts");

        webClient.post()
                .uri(expoPushUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(message)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.error("Expo push notification failed", e);
                    return Mono.empty();
                })
                .subscribe(response -> log.debug("Push notification response: {}", response));
    }

    /**
     * Get unread alert count for a doctor.
     */
    public long getUnreadAlertCount(Long doctorId) {
        return healthAlertRepository.countByDoctorIdAndIsReadFalse(doctorId);
    }

    /**
     * Get all unread alerts for a doctor.
     */
    public List<HealthAlert> getUnreadAlerts(Long doctorId) {
        return healthAlertRepository.findByDoctorIdAndIsReadFalseOrderByCreatedAtDesc(doctorId);
    }
}
