package com.medico.entity;

/**
 * Status of a disease in patient's history
 */
public enum DiseaseStatus {
    ACTIVE, // Disease is currently active/being monitored
    CLEARED, // Disease has been cleared/resolved
    MONITORING, // Disease is under observation
    CHRONIC // Chronic condition (ongoing)
}
