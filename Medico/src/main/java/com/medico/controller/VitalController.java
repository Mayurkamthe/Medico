package com.medico.controller;

import com.medico.dto.*;
import com.medico.service.VitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class VitalController {

    private final VitalService vitalService;

    @Value("${app.iot.api-key}")
    private String iotApiKey;

    /**
     * IoT endpoint - receives vital data from ESP32 devices.
     * Uses API key authentication instead of JWT.
     */
    @PostMapping("/iot/vitals")
    public ResponseEntity<ApiResponse<VitalReadingResponse>> receiveIoTVitals(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @Valid @RequestBody IoTVitalRequest request) {
        try {
            // Validate API key (optional for development, enable in production)
            // if (apiKey == null || !apiKey.equals(iotApiKey)) {
            // return ResponseEntity.status(401).body(ApiResponse.error("Invalid API key"));
            // }

            log.info("Received vitals from device: {}", request.getDeviceId());
            VitalReadingResponse response = vitalService.processIoTVitals(request);
            return ResponseEntity.ok(ApiResponse.success("Vitals recorded successfully", response));
        } catch (Exception e) {
            log.error("Error processing IoT vitals", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Check device status (polling endpoint for IoT capability).
     */
    @GetMapping("/iot/device-status/{deviceId}")
    public ResponseEntity<ApiResponse<Boolean>> checkDeviceStatus(@PathVariable String deviceId) {
        boolean active = vitalService.isDeviceActive(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Device status checked", active));
    }

    /**
     * Get vital history for a patient (paginated).
     */
    @GetMapping("/patients/{patientId}/vitals")
    public ResponseEntity<ApiResponse<Page<VitalReadingResponse>>> getVitalHistory(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<VitalReadingResponse> vitals = vitalService.getVitalHistory(patientId, pageable);
            return ResponseEntity.ok(ApiResponse.success(vitals));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get latest vitals for a patient.
     */
    @GetMapping("/patients/{patientId}/vitals/latest")
    public ResponseEntity<ApiResponse<VitalReadingResponse>> getLatestVitals(@PathVariable Long patientId) {
        try {
            VitalReadingResponse vitals = vitalService.getLatestVitals(patientId);
            return ResponseEntity.ok(ApiResponse.success(vitals));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get recent vitals (last 10) for a patient.
     */
    @GetMapping("/patients/{patientId}/vitals/recent")
    public ResponseEntity<ApiResponse<List<VitalReadingResponse>>> getRecentVitals(@PathVariable Long patientId) {
        try {
            List<VitalReadingResponse> vitals = vitalService.getRecentVitals(patientId);
            return ResponseEntity.ok(ApiResponse.success(vitals));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get vitals within a date range.
     */
    @GetMapping("/patients/{patientId}/vitals/range")
    public ResponseEntity<ApiResponse<List<VitalReadingResponse>>> getVitalsByDateRange(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<VitalReadingResponse> vitals = vitalService.getVitalsByDateRange(patientId, start, end);
            return ResponseEntity.ok(ApiResponse.success(vitals));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
