package com.medico.controller;

import com.medico.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<byte[]> downloadPatientReport(@PathVariable Long patientId) {
        try {
            byte[] reportContext = reportService.generatePatientReport(patientId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=patient_report_" + patientId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(reportContext);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
