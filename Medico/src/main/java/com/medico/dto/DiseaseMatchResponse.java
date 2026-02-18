package com.medico.dto;

import com.medico.service.DiseaseKnowledgeBase.DiseaseMatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for disease matching results
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiseaseMatchResponse {
    private String diseaseName;
    private double confidence;
    private String possibleCauses;
    private List<String> symptoms;
    private List<String> recommendations;
    private List<String> matchedParameters;

    public static DiseaseMatchResponse fromMatch(DiseaseMatch match) {
        return new DiseaseMatchResponse(
                match.getDisease().getName(),
                match.getConfidence(),
                match.getDisease().getPossibleCauses(),
                match.getDisease().getSymptoms(),
                match.getDisease().getRecommendations(),
                match.getMatchedParameters());
    }

    public static List<DiseaseMatchResponse> fromMatches(List<DiseaseMatch> matches) {
        return matches.stream()
                .map(DiseaseMatchResponse::fromMatch)
                .collect(Collectors.toList());
    }
}
