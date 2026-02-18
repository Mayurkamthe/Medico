package com.medico.controller;

import com.medico.dto.*;
import com.medico.service.DiseaseMatchingService;
import com.medico.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final DiseaseMatchingService diseaseMatchingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientResponse>>> getAllPatients() {
        try {
            List<PatientResponse> patients = patientService.getAllPatients();
            return ResponseEntity.ok(ApiResponse.success(patients));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(@PathVariable Long id) {
        try {
            PatientResponse patient = patientService.getPatientById(id);
            return ResponseEntity.ok(ApiResponse.success(patient));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PatientResponse>> createPatient(@Valid @RequestBody PatientRequest request) {
        try {
            PatientResponse patient = patientService.createPatient(request);
            return ResponseEntity.ok(ApiResponse.success("Patient created successfully", patient));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {
        try {
            PatientResponse patient = patientService.updatePatient(id, request);
            return ResponseEntity.ok(ApiResponse.success("Patient updated successfully", patient));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Long id) {
        try {
            patientService.deletePatient(id);
            return ResponseEntity.ok(ApiResponse.success("Patient deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Device Assignment Endpoints

    @PostMapping("/{id}/device/assign")
    public ResponseEntity<ApiResponse<Void>> assignDevice(
            @PathVariable Long id,
            @Valid @RequestBody DeviceAssignRequest request) {
        try {
            Long doctorId = ((com.medico.security.DoctorPrincipal) org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal()).getId();

            if (request.getDurationSeconds() != null) {
                patientService.assignDeviceWithDuration(id, request.getDeviceId(), request.getDurationSeconds(),
                        doctorId);
            } else {
                patientService.assignDevice(id, request.getDeviceId(), doctorId);
            }

            return ResponseEntity.ok(ApiResponse.success("Device assigned successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/device/unassign")
    public ResponseEntity<ApiResponse<Void>> unassignDevice(@PathVariable Long id) {
        try {
            Long doctorId = ((com.medico.security.DoctorPrincipal) org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal()).getId();

            patientService.unassignDevice(id, doctorId);
            return ResponseEntity.ok(ApiResponse.success("Device unassigned successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/device/{deviceId}/active-patient")
    public ResponseEntity<ApiResponse<PatientResponse>> getActivePatientForDevice(@PathVariable String deviceId) {
        try {
            return patientService.findActivePatientForDevice(deviceId)
                    .map(patient -> ResponseEntity.ok(ApiResponse.success(PatientResponse.fromEntity(patient))))
                    .orElse(ResponseEntity.ok(ApiResponse.error("No active patient for this device")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Disease Matching Endpoints

    /**
     * Get suspected diseases based on patient's latest vital readings.
     * Returns list of diseases with confidence scores, symptoms, and
     * recommendations.
     */
    @GetMapping("/{id}/disease-matches")
    public ResponseEntity<ApiResponse<List<DiseaseMatchResponse>>> getDiseaseMatches(@PathVariable Long id) {
        try {
            var matches = diseaseMatchingService.matchDiseasesForPatient(id);
            var response = DiseaseMatchResponse.fromMatches(matches);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
