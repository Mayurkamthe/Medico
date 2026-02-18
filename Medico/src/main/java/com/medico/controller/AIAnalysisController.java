package com.medico.controller;

import com.medico.dto.*;
import com.medico.service.AIAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AIAnalysisController {

    private final AIAnalysisService aiAnalysisService;

    /**
     * Get all AI analysis results for a patient.
     */
    @GetMapping("/patients/{patientId}/ai-analysis")
    public ResponseEntity<ApiResponse<List<AIAnalysisResponse>>> getPatientAnalyses(@PathVariable Long patientId) {
        try {
            List<AIAnalysisResponse> analyses = aiAnalysisService.getPatientAnalyses(patientId);
            return ResponseEntity.ok(ApiResponse.success(analyses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get AI analysis results paginated.
     */
    @GetMapping("/patients/{patientId}/ai-analysis/paginated")
    public ResponseEntity<ApiResponse<Page<AIAnalysisResponse>>> getPatientAnalysesPaginated(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<AIAnalysisResponse> analyses = aiAnalysisService.getPatientAnalysesPaginated(
                    patientId, PageRequest.of(page, size));
            return ResponseEntity.ok(ApiResponse.success(analyses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get latest AI analysis for a patient.
     */
    @GetMapping("/patients/{patientId}/ai-analysis/latest")
    public ResponseEntity<ApiResponse<AIAnalysisResponse>> getLatestAnalysis(@PathVariable Long patientId) {
        try {
            AIAnalysisResponse analysis = aiAnalysisService.getLatestAnalysis(patientId);
            return ResponseEntity.ok(ApiResponse.success(analysis));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Manually trigger AI analysis for a patient.
     */
    @PostMapping("/patients/{patientId}/ai-analysis/trigger")
    public ResponseEntity<ApiResponse<AIAnalysisResponse>> triggerAnalysis(@PathVariable Long patientId) {
        try {
            AIAnalysisResponse analysis = aiAnalysisService.triggerAnalysis(patientId);
            return ResponseEntity.ok(ApiResponse.success("AI analysis triggered successfully", analysis));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
