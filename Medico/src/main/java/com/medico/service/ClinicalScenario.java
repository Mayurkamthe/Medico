package com.medico.service;

import com.medico.entity.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a clinical scenario based on vital sign combinations.
 * Maps to 16 predefined medical scenarios.
 */
@Data
@AllArgsConstructor
public class ClinicalScenario {
    private int scenarioId;
    private RiskLevel riskLevel;
    private String specificCause;
    private String possibleCauses;
    private String symptoms;
    private String recommendations;

    // Normal ranges for vitals (using Fahrenheit as per medical table)
    private static final double TEMP_LOW_F = 97.0; // 36.1°C
    private static final double TEMP_VERY_LOW_F = 96.0; // 35.6°C
    private static final double TEMP_HIGH_F = 99.0; // 37.2°C
    private static final double TEMP_VERY_HIGH_F = 100.0; // 37.8°C

    private static final int HR_LOW = 60;
    private static final int HR_HIGH = 100;
    private static final int SPO2_LOW = 95;

    /**
     * Convert Celsius to Fahrenheit
     */
    public static double celsiusToFahrenheit(double celsius) {
        return (celsius * 9.0 / 5.0) + 32.0;
    }

    /**
     * Match vitals to one of 16 clinical scenarios.
     * HR and SPO2 are integers, temperature is in Celsius.
     */
    public static ClinicalScenario matchScenario(int heartRate, int spo2, double tempCelsius) {
        double tempF = celsiusToFahrenheit(tempCelsius);

        // Scenario 1: HR<60, Temp<96°F, SpO2<95
        if (heartRate < HR_LOW && tempF < TEMP_VERY_LOW_F && spo2 < SPO2_LOW) {
            return new ClinicalScenario(
                    1,
                    RiskLevel.CRITICAL,
                    "Bradycardia, Hypothermia, Hypoxemia",
                    "Cardiac issues, hypothermia, respiratory problems, or other serious conditions",
                    "Dizziness or fainting, Shortness of Breath, Chest Pain or Palpitation",
                    "1. Seek immediate medical attention. 2. Monitor vitals closely. 3. Keep warm (if hypothermic)");
        }

        // Scenario 2: HR>100, Temp<97°F, SpO2<95
        if (heartRate > HR_HIGH && tempF < TEMP_LOW_F && spo2 < SPO2_LOW) {
            return new ClinicalScenario(
                    2,
                    RiskLevel.CRITICAL,
                    "Tachycardia, Mild Hypothermia, Mild Hypoxemia",
                    "Serious underlying condition, sepsis, or shock",
                    "Dizziness, confusion, shortness of breath",
                    "1. Seek medical attention IMMEDIATELY. 2. Monitor vitals closely");
        }

        // Scenario 3: HR Normal, Temp<97°F, SpO2 Normal
        if (heartRate >= HR_LOW && heartRate <= HR_HIGH && tempF < TEMP_LOW_F && spo2 >= SPO2_LOW) {
            return new ClinicalScenario(
                    3,
                    RiskLevel.MODERATE,
                    "Mild Hypothermia",
                    "Mild hypothermia, possibly due to cold environment or other factors",
                    "Shivering, confusion, dizziness",
                    "1. Warm up with blankets or warm fluids. 2. Monitor vitals. 3. Consult a healthcare pro if symptoms persist");
        }

        // Scenario 4: HR Normal, Temp>100°F, SpO2 Normal
        if (heartRate >= HR_LOW && heartRate <= HR_HIGH && tempF > TEMP_VERY_HIGH_F && spo2 >= SPO2_LOW) {
            return new ClinicalScenario(
                    4,
                    RiskLevel.MODERATE,
                    "Mild Fever",
                    "Infection, inflammation, or other minor issues",
                    "Headache, body ache, sweating",
                    "1. Stay hydrated. 2. Rest. 3. Monitor temp; see a doctor if it spikes or persists");
        }

        // Scenario 5: HR<60, Temp Normal, SpO2 Normal
        if (heartRate < HR_LOW && tempF >= TEMP_LOW_F && tempF <= TEMP_HIGH_F && spo2 >= SPO2_LOW) {
            return new ClinicalScenario(
                    5,
                    RiskLevel.MODERATE,
                    "Bradycardia",
                    "Athletic training, medication side effect, or underlying condition",
                    "Dizziness, fatigue, fainting",
                    "1. Consult a healthcare pro to rule out underlying issues. 2. Monitor HR and symptoms");
        }

        // Scenario 6: HR>100, Temp Normal, SpO2 Normal
        if (heartRate > HR_HIGH && tempF >= TEMP_LOW_F && tempF <= TEMP_HIGH_F && spo2 >= SPO2_LOW) {
            return new ClinicalScenario(
                    6,
                    RiskLevel.MODERATE,
                    "Tachycardia",
                    "Stress, anxiety, caffeine, or underlying condition",
                    "Palpitations, shortness of breath, dizziness",
                    "1. Relax and hydrate. 2. Consult a healthcare pro if symptoms persist");
        }

        // Scenario 7: HR Normal, Temp Normal, SpO2<95
        if (heartRate >= HR_LOW && heartRate <= HR_HIGH && tempF >= TEMP_LOW_F && tempF <= TEMP_HIGH_F
                && spo2 < SPO2_LOW) {
            return new ClinicalScenario(
                    7,
                    RiskLevel.MODERATE,
                    "Mild Hypoxemia",
                    "Respiratory issues, altitude, or other factors",
                    "Shortness of breath, dizziness, headache",
                    "1. Take deep breaths and relax. 2. Consult a healthcare pro to check for underlying issues");
        }

        // Scenario 8: HR<60, Temp<97°F, SpO2 Normal
        if (heartRate < HR_LOW && tempF < TEMP_LOW_F && spo2 >= SPO2_LOW) {
            return new ClinicalScenario(
                    8,
                    RiskLevel.CRITICAL,
                    "Bradycardia, Mild Hypothermia",
                    "Underlying condition, hypothermia, or medication side effect",
                    "Dizziness, shivering, fatigue",
                    "1. Seek medical attention ASAP. 2. Warm up and monitor vitals");
        }

        // Scenario 9: HR<60, Temp>99°F, SpO2 Normal
        if (heartRate < HR_LOW && tempF > TEMP_HIGH_F && spo2 >= SPO2_LOW) {
            return new ClinicalScenario(
                    9,
                    RiskLevel.MODERATE,
                    "Bradycardia, Mild Fever",
                    "Infection, inflammation, or medication effect",
                    "Dizziness, fatigue, sweating",
                    "1. Consult a healthcare pro to check for underlying issues. 2. Monitor symptoms and stay hydrated");
        }

        // Scenario 10: HR>100, Temp<97°F, SpO2 Normal
        if (heartRate > HR_HIGH && tempF < TEMP_LOW_F && spo2 >= SPO2_LOW) {
            return new ClinicalScenario(
                    10,
                    RiskLevel.CRITICAL,
                    "Tachycardia, Mild Hypothermia",
                    "Infection, stress, or underlying condition",
                    "Palpitations, shivering, dizziness",
                    "1. Seek medical attention ASAP. 2. Warm up and monitor vitals");
        }

        // Scenario 11: HR>100, Temp>100°F, SpO2 Normal
        if (heartRate > HR_HIGH && tempF > TEMP_VERY_HIGH_F && spo2 >= SPO2_LOW) {
            return new ClinicalScenario(
                    11,
                    RiskLevel.CRITICAL,
                    "Tachycardia, Fever",
                    "Infection, inflammation, or other issues",
                    "Sweating, body ache, palpitations",
                    "1. Consult a healthcare pro ASAP. 2. Stay hydrated and rest");
        }

        // Scenario 12: HR Normal, Temp<97°F, SpO2<95
        if (heartRate >= HR_LOW && heartRate <= HR_HIGH && tempF < TEMP_LOW_F && spo2 < SPO2_LOW) {
            return new ClinicalScenario(
                    12,
                    RiskLevel.CRITICAL,
                    "Mild Hypothermia, Mild Hypoxemia",
                    "Underlying condition, environmental exposure",
                    "Shivering, shortness of breath, dizziness",
                    "1. Seek medical attention ASAP. 2. Warm up and get oxygen checked");
        }

        // Scenario 13: HR Normal, Temp>99°F, SpO2<95
        if (heartRate >= HR_LOW && heartRate <= HR_HIGH && tempF > TEMP_HIGH_F && spo2 < SPO2_LOW) {
            return new ClinicalScenario(
                    13,
                    RiskLevel.CRITICAL,
                    "Fever, Mild Hypoxemia",
                    "Respiratory infection, pneumonia, or other issues",
                    "Shortness of breath, cough, fatigue",
                    "1. Consult a healthcare pro ASAP. 2. Monitor symptoms and oxygen levels");
        }

        // Scenario 14: HR<60, Temp Normal, SpO2<95
        if (heartRate < HR_LOW && tempF >= TEMP_LOW_F && tempF <= TEMP_HIGH_F && spo2 < SPO2_LOW) {
            return new ClinicalScenario(
                    14,
                    RiskLevel.CRITICAL,
                    "Bradycardia, Mild Hypoxemia",
                    "Underlying heart or lung issue",
                    "Dizziness, fatigue, shortness of breath",
                    "1. Seek medical attention ASAP. 2. Monitor vitals and oxygen levels");
        }

        // Scenario 15: HR>100, Temp Normal, SpO2<95
        if (heartRate > HR_HIGH && tempF >= TEMP_LOW_F && tempF <= TEMP_HIGH_F && spo2 < SPO2_LOW) {
            return new ClinicalScenario(
                    15,
                    RiskLevel.CRITICAL,
                    "Tachycardia, Mild Hypoxemia",
                    "Respiratory issues, anxiety, or underlying condition",
                    "Palpitations, shortness of breath, dizziness",
                    "1. Consult a healthcare pro ASAP. 2. Take deep breaths and relax");
        }

        // Scenario 16: HR>100, Temp<97°F, SpO2<95 (duplicate of 2, but keeping for
        // completeness)
        if (heartRate > HR_HIGH && tempF < TEMP_LOW_F && spo2 < SPO2_LOW) {
            return new ClinicalScenario(
                    16,
                    RiskLevel.CRITICAL,
                    "Tachycardia, Mild Hypothermia, Mild Hypoxemia",
                    "Serious underlying condition, sepsis, or shock",
                    "Dizziness, confusion, shortness of breath",
                    "1. Seek medical attention IMMEDIATELY. 2. Monitor vitals closely");
        }

        // Default: All normal (shouldn't reach here if vitals are truly normal)
        return new ClinicalScenario(
                0,
                RiskLevel.NORMAL,
                "All vitals normal",
                "Healthy status",
                "None",
                "Continue regular monitoring");
    }
}
