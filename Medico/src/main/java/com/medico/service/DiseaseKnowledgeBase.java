package com.medico.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Comprehensive Disease Knowledge Base containing 12 common diseases
 * with their symptoms, vital parameter thresholds, and recommendations.
 * Based on medical guidelines for IoT-based health monitoring.
 */
public class DiseaseKnowledgeBase {

    private static final List<DiseaseInfo> DISEASES = new ArrayList<>();

    static {
        // 1. Cough, Cold, and Sore Throat
        DISEASES.add(new DiseaseInfo(
                1,
                "Cough, Cold, and Sore Throat",
                "Usually viral (e.g., flu, COVID-19) or bacterial infections, allergies, or irritants",
                new VitalThresholds(38.0, null, 60, 100, 95, null, 24, null),
                Arrays.asList(
                        "Cough (dry/productive)",
                        "Runny/stuffy nose",
                        "Sore, scratchy throat",
                        "Fatigue",
                        "Headache",
                        "Body aches"),
                Arrays.asList(
                        "Rest and hydration",
                        "Saline gargles or throat lozenges for sore throat",
                        "Over-the-counter meds (e.g., paracetamol, decongestants)",
                        "Humidifier to ease congestion",
                        "Consult doctor if symptoms persist beyond 7 days")));

        // 2. Fever
        DISEASES.add(new DiseaseInfo(
                2,
                "Fever",
                "Usually an infection (viral or bacterial), inflammation, or immune response",
                new VitalThresholds(38.0, null, null, 100, null, null, null, null),
                Arrays.asList(
                        "Elevated body temperature",
                        "Chills, sweating",
                        "Headache",
                        "Body aches",
                        "Fatigue",
                        "Loss of appetite"),
                Arrays.asList(
                        "Monitor temperature regularly",
                        "Stay hydrated and rest",
                        "Paracetamol or ibuprofen (consult dosage)",
                        "Consult a doctor if fever >40°C (104°F) or persists >3 days")));

        // 3. Diarrhea
        DISEASES.add(new DiseaseInfo(
                3,
                "Diarrhea",
                "Usually due to infections (viral, bacterial, parasitic), food intolerance, or medications",
                new VitalThresholds(38.0, null, null, 100, 95, null, null, null),
                Arrays.asList(
                        "Frequent, loose stools",
                        "Abdominal cramps and pain",
                        "Nausea, vomiting",
                        "Dehydration (thirst, dizziness)"),
                Arrays.asList(
                        "Stay hydrated with ORS (Oral Rehydration Solution)",
                        "Bland diet (e.g., bananas, rice, toast)",
                        "Avoid spicy, oily foods",
                        "Consult a doctor if severe dehydration, blood in stool, or persists >2 days")));

        // 4. Malaria
        DISEASES.add(new DiseaseInfo(
                4,
                "Malaria",
                "Mosquito-borne Plasmodium parasite (e.g., P. falciparum, P. vivax)",
                new VitalThresholds(38.0, null, null, 100, 95, null, null, null),
                Arrays.asList(
                        "Fever with chills and sweating",
                        "Headache",
                        "Body aches",
                        "Nausea, vomiting",
                        "Fatigue, weakness"),
                Arrays.asList(
                        "Get diagnosed with a blood test (required for confirmation)",
                        "Antimalarial medications (e.g., ACTs) as prescribed",
                        "Rest and hydration",
                        "Use mosquito nets and repellents for prevention")));

        // 5. Chikungunya
        DISEASES.add(new DiseaseInfo(
                5,
                "Chikungunya",
                "Mosquito-borne viral infection (Aedes aegypti/albopictus)",
                new VitalThresholds(38.5, null, null, 100, null, null, null, null),
                Arrays.asList(
                        "Sudden high fever",
                        "Severe joint pain and swelling",
                        "Rash",
                        "Headache",
                        "Fatigue",
                        "Muscle pain"),
                Arrays.asList(
                        "Rest and hydration",
                        "Pain relief meds (e.g., paracetamol) - avoid aspirin/NSAIDs initially",
                        "Anti-inflammatory meds (consult doctor)",
                        "Mosquito control and protective clothing")));

        // 6. Hepatitis
        DISEASES.add(new DiseaseInfo(
                6,
                "Hepatitis",
                "Viral infections (Hep A, B, C, D, E), toxins, or autoimmune issues",
                new VitalThresholds(37.5, 38.5, null, null, null, null, null, null),
                Arrays.asList(
                        "Jaundice (yellowing eyes/skin)",
                        "Fatigue",
                        "Abdominal pain",
                        "Nausea, loss of appetite",
                        "Dark urine",
                        "Pale stools"),
                Arrays.asList(
                        "Consult a hepatologist immediately",
                        "Rest and hydration",
                        "Antivirals (if viral hepatitis, as prescribed)",
                        "Avoid alcohol and fatty foods",
                        "Liver function tests recommended")));

        // 7. Cholera
        DISEASES.add(new DiseaseInfo(
                7,
                "Cholera",
                "Bacterial infection (Vibrio cholerae) from contaminated food/water",
                new VitalThresholds(null, 38.0, null, 100, 95, null, null, null),
                Arrays.asList(
                        "Profuse watery diarrhea (rice-water stools)",
                        "Vomiting",
                        "Severe dehydration",
                        "Abdominal cramps",
                        "Thirst, weakness"),
                Arrays.asList(
                        "ORS (Oral Rehydration Solution) immediately",
                        "IV fluids if severe dehydration",
                        "Antibiotics (consult doctor)",
                        "Maintain hygiene and use safe water",
                        "Seek emergency care if severe")));

        // 8. Pneumonia
        DISEASES.add(new DiseaseInfo(
                8,
                "Pneumonia",
                "Bacterial (e.g., Streptococcus pneumoniae), viral, or fungal lung infection",
                new VitalThresholds(38.0, null, null, 100, 95, null, 20, null),
                Arrays.asList(
                        "Cough with phlegm",
                        "Fever, chills",
                        "Chest pain",
                        "Breathing difficulty",
                        "Fatigue, weakness"),
                Arrays.asList(
                        "Consult a pulmonologist immediately",
                        "Antibiotics if bacterial (as prescribed)",
                        "Oxygen therapy if low SpO2",
                        "Rest and hydration",
                        "Chest X-ray may be required")));

        // 9. Headache
        DISEASES.add(new DiseaseInfo(
                9,
                "Headache",
                "Tension, migraines, sinus issues, dehydration, stress, or underlying conditions",
                new VitalThresholds(38.0, null, null, null, null, null, null, null),
                Arrays.asList(
                        "Throbbing/pulsating pain",
                        "Sensitivity to light/sound",
                        "Nausea, dizziness"),
                Arrays.asList(
                        "Identify triggers (e.g., stress, food, sleep)",
                        "Pain relief meds (e.g., paracetamol, ibuprofen)",
                        "Hydration and relaxation techniques",
                        "Consult a doctor if severe or persistent")));

        // 10. Body Ache
        DISEASES.add(new DiseaseInfo(
                10,
                "Body Ache",
                "Viral infections (e.g., flu), muscle strain, dehydration, or underlying conditions",
                new VitalThresholds(38.0, null, null, null, null, null, null, null),
                Arrays.asList(
                        "Generalized muscle pain",
                        "Fatigue, weakness",
                        "Joint discomfort"),
                Arrays.asList(
                        "Rest and hydration",
                        "Pain relief meds (e.g., paracetamol)",
                        "Warm compresses, gentle stretches",
                        "Consult a doctor if severe or persistent")));

        // 11. Typhoid
        DISEASES.add(new DiseaseInfo(
                11,
                "Typhoid",
                "Bacterial infection (Salmonella Typhi) from contaminated food/water",
                new VitalThresholds(39.0, null, 50, 70, null, null, null, null), // Low HR is characteristic
                Arrays.asList(
                        "High fever (may spike to 104°F/40°C)",
                        "Abdominal pain",
                        "Headache",
                        "Weakness, loss of appetite",
                        "Rash (rose spots in some cases)"),
                Arrays.asList(
                        "Antibiotics (consult doctor) - required treatment",
                        "Hydration and rest",
                        "Widal test or blood culture for diagnosis",
                        "Maintain hygiene and safe food/water",
                        "Complete the full antibiotic course")));

        // 12. Dengue
        DISEASES.add(new DiseaseInfo(
                12,
                "Dengue",
                "Spread by Aedes mosquito bite, Viral Infection (DENV)",
                new VitalThresholds(38.0, null, null, null, null, null, null, null), // HR can be low or elevated
                Arrays.asList(
                        "High fever",
                        "Severe headache",
                        "Pain behind eyes",
                        "Joint and muscle pain",
                        "Rash",
                        "Bleeding tendency (in severe cases)"),
                Arrays.asList(
                        "Hydrate with lots of fluids (coconut water, ORS)",
                        "Rest - avoid exertion",
                        "Monitor platelet count, PCV, and blood pressure",
                        "Seek medical care ASAP if bleeding or signs of shock",
                        "Avoid aspirin and NSAIDs (use paracetamol only)",
                        "Blood test (NS1 antigen, dengue IgM/IgG) for confirmation")));
    }

    /**
     * Get all diseases in the knowledge base
     */
    public static List<DiseaseInfo> getAllDiseases() {
        return Collections.unmodifiableList(DISEASES);
    }

    /**
     * Get a disease by its ID
     */
    public static Optional<DiseaseInfo> getDiseaseById(int id) {
        return DISEASES.stream().filter(d -> d.getId() == id).findFirst();
    }

    /**
     * Get a disease by name (case-insensitive partial match)
     */
    public static Optional<DiseaseInfo> getDiseaseByName(String name) {
        return DISEASES.stream()
                .filter(d -> d.getName().toLowerCase().contains(name.toLowerCase()))
                .findFirst();
    }

    /**
     * Match diseases based on vital readings.
     * Returns list of diseases that match the vital thresholds with confidence
     * scores.
     */
    public static List<DiseaseMatch> matchDiseases(double temperature, int heartRate, int spo2,
            Integer respiratoryRate) {
        List<DiseaseMatch> matches = new ArrayList<>();

        for (DiseaseInfo disease : DISEASES) {
            VitalThresholds thresholds = disease.getThresholds();
            int matchScore = 0;
            int totalChecks = 0;
            List<String> matchedParameters = new ArrayList<>();

            // Check temperature
            if (thresholds.getTempMin() != null || thresholds.getTempMax() != null) {
                totalChecks++;
                boolean tempMatch = false;
                if (thresholds.getTempMin() != null && temperature >= thresholds.getTempMin()) {
                    tempMatch = true;
                }
                if (thresholds.getTempMax() != null && temperature <= thresholds.getTempMax()) {
                    tempMatch = true;
                }
                if (thresholds.getTempMin() != null && thresholds.getTempMax() == null
                        && temperature >= thresholds.getTempMin()) {
                    tempMatch = true;
                }
                if (tempMatch) {
                    matchScore++;
                    matchedParameters.add("Temperature: " + temperature + "°C");
                }
            }

            // Check heart rate
            if (thresholds.getHrMin() != null || thresholds.getHrMax() != null) {
                totalChecks++;
                boolean hrMatch = false;
                if (thresholds.getHrMin() != null && thresholds.getHrMax() != null) {
                    hrMatch = heartRate >= thresholds.getHrMin() && heartRate <= thresholds.getHrMax();
                } else if (thresholds.getHrMax() != null && heartRate > thresholds.getHrMax()) {
                    hrMatch = true;
                } else if (thresholds.getHrMin() != null && heartRate < thresholds.getHrMin()) {
                    hrMatch = true;
                }
                if (hrMatch) {
                    matchScore++;
                    matchedParameters.add("Heart Rate: " + heartRate + " bpm");
                }
            }

            // Check SpO2
            if (thresholds.getSpo2Min() != null) {
                totalChecks++;
                if (spo2 < thresholds.getSpo2Min()) {
                    matchScore++;
                    matchedParameters.add("SpO2: " + spo2 + "%");
                }
            }

            // Check respiratory rate (if available)
            if (respiratoryRate != null && thresholds.getRrMin() != null) {
                totalChecks++;
                if (respiratoryRate > thresholds.getRrMin()) {
                    matchScore++;
                    matchedParameters.add("Respiratory Rate: " + respiratoryRate + " breaths/min");
                }
            }

            // Calculate confidence score
            if (totalChecks > 0 && matchScore > 0) {
                double confidence = (double) matchScore / totalChecks * 100;
                // Only include if at least 50% match
                if (confidence >= 50) {
                    matches.add(new DiseaseMatch(disease, confidence, matchedParameters));
                }
            }
        }

        // Sort by confidence (highest first)
        matches.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));

        return matches;
    }

    /**
     * Disease information structure
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DiseaseInfo {
        private int id;
        private String name;
        private String possibleCauses;
        private VitalThresholds thresholds;
        private List<String> symptoms;
        private List<String> recommendations;
    }

    /**
     * Vital sign thresholds for disease matching
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VitalThresholds {
        private Double tempMin; // Minimum temperature threshold (°C)
        private Double tempMax; // Maximum temperature threshold (°C)
        private Integer hrMin; // Minimum heart rate threshold
        private Integer hrMax; // Maximum heart rate threshold
        private Integer spo2Min; // SpO2 threshold (below this is concerning)
        private Integer spo2Max; // SpO2 max (usually null)
        private Integer rrMin; // Respiratory rate threshold (above this is concerning)
        private Integer rrMax; // Respiratory rate max
    }

    /**
     * Disease match result with confidence score
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DiseaseMatch {
        private DiseaseInfo disease;
        private double confidence;
        private List<String> matchedParameters;
    }
}
