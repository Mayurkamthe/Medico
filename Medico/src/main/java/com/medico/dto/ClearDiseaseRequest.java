package com.medico.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for clearing a disease from patient
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClearDiseaseRequest {
    private String clearanceNotes;
}
