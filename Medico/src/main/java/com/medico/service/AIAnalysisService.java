package com.medico.service;

import com.medico.dto.AIAnalysisResponse;
import com.medico.entity.AIAnalysisResult;
import com.medico.entity.Patient;
import com.medico.entity.VitalReading;
import com.medico.repository.AIAnalysisResultRepository;
import com.medico.repository.PatientRepository;
import com.medico.repository.VitalReadingRepository;
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
public class AIAnalysisService {

    private final AIAnalysisResultRepository aiAnalysisResultRepository;
    private final PatientRepository patientRepository;
    private final VitalReadingRepository vitalReadingRepository;
    private final GeminiAIService geminiAIService;

    private Long getCurrentDoctorId() {
        DoctorPrincipal principal = (DoctorPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.getId();
    }

    @Transactional(readOnly = true)
    public List<AIAnalysisResponse> getPatientAnalyses(Long patientId) {
        // Verify doctor has access to this patient
        Long doctorId = getCurrentDoctorId();
        patientRepository.findByIdAndAssignedDoctorId(patientId, doctorId)
                .orElseThrow(() -> new RuntimeException("Patient not found or access denied"));

        List<AIAnalysisResult> analyses = aiAnalysisResultRepository.findByPatientIdOrderByAnalyzedAtDesc(patientId);
        return analyses.stream()
                .map(AIAnalysisResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AIAnalysisResponse> getPatientAnalysesPaginated(Long patientId, Pageable pageable) {
        Long doctorId = getCurrentDoctorId();
        patientRepository.findByIdAndAssignedDoctorId(patientId, doctorId)
                .orElseThrow(() -> new RuntimeException("Patient not found or access denied"));

        Page<AIAnalysisResult> analyses = aiAnalysisResultRepository.findByPatientIdOrderByAnalyzedAtDesc(patientId,
                pageable);
        return analyses.map(AIAnalysisResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public AIAnalysisResponse getLatestAnalysis(Long patientId) {
        Long doctorId = getCurrentDoctorId();
        patientRepository.findByIdAndAssignedDoctorId(patientId, doctorId)
                .orElseThrow(() -> new RuntimeException("Patient not found or access denied"));

        AIAnalysisResult analysis = aiAnalysisResultRepository.findFirstByPatientIdOrderByAnalyzedAtDesc(patientId)
                .orElseThrow(() -> new RuntimeException("No AI analysis found for patient"));

        return AIAnalysisResponse.fromEntity(analysis);
    }

    /**
     * Manually trigger AI analysis for a patient's latest vitals.
     */
    @Transactional
    public AIAnalysisResponse triggerAnalysis(Long patientId) {
        Long doctorId = getCurrentDoctorId();
        Patient patient = patientRepository.findByIdAndAssignedDoctorId(patientId, doctorId)
                .orElseThrow(() -> new RuntimeException("Patient not found or access denied"));

        VitalReading latestVitals = vitalReadingRepository.findFirstByPatientIdOrderByRecordedAtDesc(patientId)
                .orElseThrow(() -> new RuntimeException("No vital readings found for patient"));

        AIAnalysisResult result = geminiAIService.analyzeVitals(patient, latestVitals);
        return AIAnalysisResponse.fromEntity(result);
    }
}
