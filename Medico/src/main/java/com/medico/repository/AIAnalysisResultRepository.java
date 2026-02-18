package com.medico.repository;

import com.medico.entity.AIAnalysisResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIAnalysisResultRepository extends JpaRepository<AIAnalysisResult, Long> {
    List<AIAnalysisResult> findByPatientIdOrderByAnalyzedAtDesc(Long patientId);

    Page<AIAnalysisResult> findByPatientIdOrderByAnalyzedAtDesc(Long patientId, Pageable pageable);

    Optional<AIAnalysisResult> findFirstByPatientIdOrderByAnalyzedAtDesc(Long patientId);

    Optional<AIAnalysisResult> findByVitalReadingId(Long vitalReadingId);
}
