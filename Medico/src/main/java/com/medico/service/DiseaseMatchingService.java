package com.medico.service;

import com.medico.entity.Patient;
import com.medico.entity.VitalReading;
import com.medico.repository.VitalReadingRepository;
import com.medico.service.DiseaseKnowledgeBase.DiseaseMatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for matching patient vitals to potential diseases.
 * Combines IoT vital readings with the disease knowledge base
 * to provide clinical insights.
 */
@Service
@RequiredArgsConstructor
public class DiseaseMatchingService {

    private final VitalReadingRepository vitalReadingRepository;

    /**
     * Match a patient's latest vitals against the disease knowledge base.
     * 
     * @param patientId The patient ID
     * @return List of disease matches with confidence scores
     */
    public List<DiseaseMatch> matchDiseasesForPatient(Long patientId) {
        VitalReading latestVitals = vitalReadingRepository
                .findFirstByPatientIdOrderByRecordedAtDesc(patientId)
                .orElse(null);

        if (latestVitals == null) {
            return List.of();
        }

        return matchDiseases(latestVitals);
    }

    /**
     * Match specific vital readings against diseases.
     * 
     * @param vitals The vital reading to analyze
     * @return List of disease matches with confidence scores
     */
    public List<DiseaseMatch> matchDiseases(VitalReading vitals) {
        return DiseaseKnowledgeBase.matchDiseases(
                vitals.getTemperature(),
                vitals.getHeartRate(),
                vitals.getSpo2(),
                null // Respiratory rate not currently tracked by IoT device
        );
    }

    /**
     * Match diseases based on raw vital values.
     * 
     * @param temperature     Body temperature in Celsius
     * @param heartRate       Heart rate in bpm
     * @param spo2            Oxygen saturation percentage
     * @param respiratoryRate Respiratory rate (optional, can be null)
     * @return List of disease matches
     */
    public List<DiseaseMatch> matchDiseases(double temperature, int heartRate, int spo2, Integer respiratoryRate) {
        return DiseaseKnowledgeBase.matchDiseases(temperature, heartRate, spo2, respiratoryRate);
    }

    /**
     * Get a summary of matched diseases for display.
     * Formats the disease matches into a human-readable summary.
     * 
     * @param matches List of disease matches
     * @return Formatted summary string
     */
    public String getMatchSummary(List<DiseaseMatch> matches) {
        if (matches.isEmpty()) {
            return "Vitals appear within normal ranges. Continue regular monitoring.";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("Based on the current vital readings, the following conditions should be considered:\n\n");

        for (int i = 0; i < Math.min(matches.size(), 3); i++) {
            DiseaseMatch match = matches.get(i);
            summary.append(String.format("%d. %s (%.0f%% match)\n",
                    i + 1,
                    match.getDisease().getName(),
                    match.getConfidence()));
            summary.append("   Matched parameters: ")
                    .append(String.join(", ", match.getMatchedParameters()))
                    .append("\n");
        }

        return summary.toString();
    }

    /**
     * Get symptoms for all matched diseases.
     * 
     * @param matches List of disease matches
     * @return Combined list of unique symptoms
     */
    public List<String> getMatchedSymptoms(List<DiseaseMatch> matches) {
        return matches.stream()
                .flatMap(m -> m.getDisease().getSymptoms().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Get recommendations for all matched diseases.
     * 
     * @param matches List of disease matches
     * @return Combined list of unique recommendations
     */
    public List<String> getMatchedRecommendations(List<DiseaseMatch> matches) {
        return matches.stream()
                .flatMap(m -> m.getDisease().getRecommendations().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Determine if any matched disease requires urgent attention.
     * 
     * @param matches List of disease matches
     * @return true if urgent conditions are detected
     */
    public boolean hasUrgentConditions(List<DiseaseMatch> matches) {
        // Conditions that typically require urgent care
        List<String> urgentDiseases = List.of(
                "Malaria", "Cholera", "Pneumonia", "Dengue", "Typhoid");

        return matches.stream()
                .filter(m -> m.getConfidence() >= 70)
                .anyMatch(m -> urgentDiseases.stream()
                        .anyMatch(urgent -> m.getDisease().getName().contains(urgent)));
    }

    /**
     * Build a clinical assessment based on matches.
     * 
     * @param matches Disease matches
     * @param patient Patient information
     * @return Clinical assessment summary
     */
    public ClinicalAssessment buildAssessment(List<DiseaseMatch> matches, Patient patient) {
        ClinicalAssessment assessment = new ClinicalAssessment();
        assessment.setPatientName(patient.getFullName());
        assessment.setPatientAge(patient.getAge());
        assessment.setDiseaseMatches(matches);
        assessment.setSymptoms(getMatchedSymptoms(matches));
        assessment.setRecommendations(getMatchedRecommendations(matches));
        assessment.setUrgent(hasUrgentConditions(matches));

        if (matches.isEmpty()) {
            assessment.setAssessmentNote("All vitals within normal parameters. No specific conditions detected.");
        } else if (assessment.isUrgent()) {
            assessment.setAssessmentNote(
                    "ATTENTION REQUIRED: Vital signs indicate possible serious condition. Please evaluate and consider appropriate tests.");
        } else {
            assessment.setAssessmentNote(
                    "Abnormal vitals detected. Monitor closely and consider further evaluation if symptoms persist.");
        }

        return assessment;
    }

    /**
     * Clinical assessment result structure
     */
    @lombok.Data
    public static class ClinicalAssessment {
        private String patientName;
        private int patientAge;
        private List<DiseaseMatch> diseaseMatches;
        private List<String> symptoms;
        private List<String> recommendations;
        private boolean urgent;
        private String assessmentNote;
    }
}
