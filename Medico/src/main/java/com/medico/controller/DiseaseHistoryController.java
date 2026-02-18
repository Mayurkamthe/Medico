package com.medico.controller;

import com.medico.dto.*;
import com.medico.entity.DiseaseStatus;
import com.medico.service.DiseaseHistoryService;
import com.medico.service.DiseaseKnowledgeBase.DiseaseInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for patient disease history management
 */
@RestController
@RequestMapping("/api/patients/{patientId}/disease-history")
@RequiredArgsConstructor
public class DiseaseHistoryController {

    private final DiseaseHistoryService diseaseHistoryService;

    /**
     * Get all disease history for a patient
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DiseaseHistoryResponse>>> getDiseaseHistory(@PathVariable Long patientId) {
        try {
            List<DiseaseHistoryResponse> history = diseaseHistoryService.getPatientDiseaseHistory(patientId);
            return ResponseEntity.ok(ApiResponse.success(history));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get paginated disease history
     */
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<DiseaseHistoryResponse>>> getDiseaseHistoryPaginated(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<DiseaseHistoryResponse> history = diseaseHistoryService.getPatientDiseaseHistoryPaginated(patientId,
                    page, size);
            return ResponseEntity.ok(ApiResponse.success(history));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get disease history summary
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DiseaseHistoryService.DiseaseHistorySummary>> getDiseaseSummary(
            @PathVariable Long patientId) {
        try {
            DiseaseHistoryService.DiseaseHistorySummary summary = diseaseHistoryService
                    .getPatientDiseaseSummary(patientId);
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get active diseases only
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<DiseaseHistoryResponse>>> getActiveDiseases(@PathVariable Long patientId) {
        try {
            List<DiseaseHistoryResponse> active = diseaseHistoryService.getActiveDiseases(patientId);
            return ResponseEntity.ok(ApiResponse.success(active));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Record a disease manually
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DiseaseHistoryResponse>> recordDisease(
            @PathVariable Long patientId,
            @Valid @RequestBody RecordDiseaseRequest request) {
        try {
            DiseaseHistoryResponse response = diseaseHistoryService.recordDiseaseManually(patientId, request);
            return ResponseEntity.ok(ApiResponse.success("Disease recorded successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Auto-record diseases from current vital matches
     */
    @PostMapping("/auto-record")
    public ResponseEntity<ApiResponse<List<DiseaseHistoryResponse>>> autoRecordDiseases(@PathVariable Long patientId) {
        try {
            List<DiseaseHistoryResponse> recorded = diseaseHistoryService.autoRecordFromMatches(patientId);
            return ResponseEntity.ok(ApiResponse.success("Recorded " + recorded.size() + " disease(s)", recorded));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Clear/resolve a disease
     */
    @PostMapping("/{historyId}/clear")
    public ResponseEntity<ApiResponse<DiseaseHistoryResponse>> clearDisease(
            @PathVariable Long patientId,
            @PathVariable Long historyId,
            @RequestBody ClearDiseaseRequest request) {
        try {
            DiseaseHistoryResponse response = diseaseHistoryService.clearDisease(historyId, request);
            return ResponseEntity.ok(ApiResponse.success("Disease cleared successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update disease status
     */
    @PutMapping("/{historyId}/status")
    public ResponseEntity<ApiResponse<DiseaseHistoryResponse>> updateDiseaseStatus(
            @PathVariable Long patientId,
            @PathVariable Long historyId,
            @RequestParam DiseaseStatus status,
            @RequestParam(required = false) String notes) {
        try {
            DiseaseHistoryResponse response = diseaseHistoryService.updateDiseaseStatus(historyId, status, notes);
            return ResponseEntity.ok(ApiResponse.success("Status updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Add notes to disease record
     */
    @PostMapping("/{historyId}/notes")
    public ResponseEntity<ApiResponse<DiseaseHistoryResponse>> addNotes(
            @PathVariable Long patientId,
            @PathVariable Long historyId,
            @RequestBody String notes) {
        try {
            DiseaseHistoryResponse response = diseaseHistoryService.addNotes(historyId, notes);
            return ResponseEntity.ok(ApiResponse.success("Notes added successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all available diseases from knowledge base
     */
    @GetMapping("/available-diseases")
    public ResponseEntity<ApiResponse<List<DiseaseInfo>>> getAvailableDiseases(@PathVariable Long patientId) {
        try {
            List<DiseaseInfo> diseases = diseaseHistoryService.getAllDiseases();
            return ResponseEntity.ok(ApiResponse.success(diseases));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
