package com.medico.controller;

import com.medico.dto.*;
import com.medico.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<HealthAlertResponse>>> getAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<HealthAlertResponse> alerts = alertService.getAlerts(PageRequest.of(page, size));
            return ResponseEntity.ok(ApiResponse.success(alerts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<HealthAlertResponse>>> getUnreadAlerts() {
        try {
            List<HealthAlertResponse> alerts = alertService.getUnreadAlerts();
            return ResponseEntity.ok(ApiResponse.success(alerts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        try {
            long count = alertService.getUnreadCount();
            return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        try {
            alertService.markAsRead(id);
            return ResponseEntity.ok(ApiResponse.success("Alert marked as read", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        try {
            alertService.markAllAsRead();
            return ResponseEntity.ok(ApiResponse.success("All alerts marked as read", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<HealthAlertResponse>>> getPatientAlerts(@PathVariable Long patientId) {
        try {
            List<HealthAlertResponse> alerts = alertService.getPatientAlerts(patientId);
            return ResponseEntity.ok(ApiResponse.success(alerts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
