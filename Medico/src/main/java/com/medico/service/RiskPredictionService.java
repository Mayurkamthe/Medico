package com.medico.service;

import com.medico.entity.RiskLevel;
import com.medico.entity.VitalReading;
import org.springframework.stereotype.Service;

/**
 * Advanced health risk prediction engine with clinical scenario matching.
 * Analyzes patient vitals and returns risk level with detailed recommendations.
 */
@Service
public class RiskPredictionService {

    /**
     * Match vitals to a specific clinical scenario and get detailed
     * recommendations.
     */
    public ClinicalScenario matchClinicalScenario(Integer heartRate, Integer spo2, Double temperature) {
        return ClinicalScenario.matchScenario(heartRate, spo2, temperature);
    }

    /**
     * Match vitals from VitalReading entity.
     */
    public ClinicalScenario matchClinicalScenario(VitalReading vitalReading) {
        return matchClinicalScenario(
                vitalReading.getHeartRate(),
                vitalReading.getSpo2(),
                vitalReading.getTemperature());
    }

    /**
     * Predict risk level based on vital signs (backward compatibility).
     * Uses scenario matching to determine risk.
     */
    public RiskLevel predictRisk(Integer heartRate, Integer spo2, Double temperature) {
        ClinicalScenario scenario = matchClinicalScenario(heartRate, spo2, temperature);
        return scenario.getRiskLevel();
    }

    public RiskLevel predictRisk(VitalReading vitalReading) {
        return predictRisk(
                vitalReading.getHeartRate(),
                vitalReading.getSpo2(),
                vitalReading.getTemperature());
    }

    /**
     * Generate a human-readable summary of the risk factors.
     */
    public String getRiskSummary(Integer heartRate, Integer spo2, Double temperature) {
        ClinicalScenario scenario = matchClinicalScenario(heartRate, spo2, temperature);
        return String.format("Scenario %d: %s - %s",
                scenario.getScenarioId(),
                scenario.getSpecificCause(),
                scenario.getPossibleCauses());
    }
}
