package com.medico.dto;

import com.medico.entity.AIAnalysisResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIAnalysisResponse {
    private Long id;
    private Long patientId;
    private Long vitalReadingId;
    private List<String> possibleConditions;
    private String severity;
    private String recommendation;
    private String disclaimer;
    private LocalDateTime analyzedAt;

    public static AIAnalysisResponse fromEntity(AIAnalysisResult result) {
        List<String> conditions = null;
        if (result.getPossibleConditions() != null && !result.getPossibleConditions().isEmpty()) {
            try {
                // Parse JSON array string to List
                String conditionsStr = result.getPossibleConditions();
                conditionsStr = conditionsStr.replace("[", "").replace("]", "").replace("\"", "");
                conditions = List.of(conditionsStr.split(",\\s*"));
            } catch (Exception e) {
                conditions = List.of(result.getPossibleConditions());
            }
        }

        return AIAnalysisResponse.builder()
                .id(result.getId())
                .patientId(result.getPatient().getId())
                .vitalReadingId(result.getVitalReading() != null ? result.getVitalReading().getId() : null)
                .possibleConditions(conditions)
                .severity(result.getSeverity())
                .recommendation(result.getRecommendation())
                .disclaimer(result.getDisclaimer())
                .analyzedAt(result.getAnalyzedAt())
                .build();
    }
}
