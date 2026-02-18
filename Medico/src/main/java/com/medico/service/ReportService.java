package com.medico.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.medico.dto.VitalReadingResponse;
import com.medico.entity.Patient;
import com.medico.entity.RiskLevel;
import com.medico.entity.VitalReading;
import com.medico.repository.PatientRepository;
import com.medico.repository.VitalReadingRepository;
import com.medico.service.DiseaseKnowledgeBase.DiseaseMatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PatientRepository patientRepository;
    private final VitalService vitalService;
    private final VitalReadingRepository vitalReadingRepository;
    private final DiseaseMatchingService diseaseMatchingService;

    public byte[] generatePatientReport(Long patientId) throws IOException {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Fetch Data
        List<VitalReadingResponse> recentVitals = vitalService.getRecentVitals(patientId);

        // Get disease matches based on latest vitals
        List<DiseaseMatch> diseaseMatches = diseaseMatchingService.matchDiseasesForPatient(patientId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(25, 118, 210));
            Paragraph title = new Paragraph("Medico Patient Health Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Subtitle with generation time
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Paragraph subtitle = new Paragraph(
                    "Generated: "
                            + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);
            document.add(new Paragraph("\n"));

            // Patient Info Section
            addSectionHeader(document, "Patient Information");
            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(15f);
            infoTable.setWidths(new float[] { 1.5f, 2f, 1.5f, 2f });

            addInfoCell(infoTable, "Name:", patient.getFullName());
            addInfoCell(infoTable, "Code:", patient.getPatientCode());
            addInfoCell(infoTable, "Age:", String.valueOf(patient.getAge()));
            addInfoCell(infoTable, "Gender:", patient.getGender());
            addInfoCell(infoTable, "Blood Group:", patient.getBloodGroup());
            addInfoCell(infoTable, "Risk Level:", String.valueOf(patient.getCurrentRiskLevel()));

            document.add(infoTable);

            // Risk Assessment Box
            addRiskAssessmentBox(document, patient.getCurrentRiskLevel());

            // Disease Matching Section (NEW)
            if (!diseaseMatches.isEmpty()) {
                addSectionHeader(document, "Suspected Conditions Based on Vitals");
                addDiseaseMatchesSection(document, diseaseMatches);
            }

            // Symptoms to Monitor Section (NEW)
            if (!diseaseMatches.isEmpty()) {
                List<String> symptoms = diseaseMatchingService.getMatchedSymptoms(diseaseMatches);
                if (!symptoms.isEmpty()) {
                    addSectionHeader(document, "Symptoms to Monitor");
                    addBulletList(document, symptoms, new Color(255, 152, 0));
                }
            }

            // Recommendations Section (NEW)
            if (!diseaseMatches.isEmpty()) {
                List<String> recommendations = diseaseMatchingService.getMatchedRecommendations(diseaseMatches);
                if (!recommendations.isEmpty()) {
                    addSectionHeader(document, "Clinical Recommendations");
                    addNumberedList(document, recommendations, new Color(76, 175, 80));
                }
            }

            // Vitals Table
            addSectionHeader(document, "Recent Vital Signs History");
            addVitalsTable(document, recentVitals);

            // Footer / Disclaimer
            addFooter(document, diseaseMatchingService.hasUrgentConditions(diseaseMatches));

            document.close();
            return out.toByteArray();
        }
    }

    private void addSectionHeader(Document document, String title) throws DocumentException {
        document.add(new Paragraph("\n"));
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(33, 33, 33));
        Paragraph section = new Paragraph(title, sectionFont);
        section.setSpacingAfter(8f);
        document.add(section);
    }

    private void addRiskAssessmentBox(Document document, RiskLevel risk) throws DocumentException {
        Color bgColor;
        Color textColor = Color.WHITE;

        switch (risk) {
            case CRITICAL:
                bgColor = new Color(244, 67, 54); // Red
                break;
            case MODERATE:
                bgColor = new Color(255, 152, 0); // Orange
                break;
            default:
                bgColor = new Color(76, 175, 80); // Green
                break;
        }

        PdfPTable riskTable = new PdfPTable(1);
        riskTable.setWidthPercentage(100);
        riskTable.setSpacingAfter(15f);

        Font riskFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, textColor);
        PdfPCell riskCell = new PdfPCell(new Phrase("Current Risk Assessment: " + risk, riskFont));
        riskCell.setBackgroundColor(bgColor);
        riskCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        riskCell.setPadding(10);
        riskCell.setBorder(Rectangle.NO_BORDER);
        riskTable.addCell(riskCell);

        document.add(riskTable);
    }

    private void addDiseaseMatchesSection(Document document, List<DiseaseMatch> matches) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10f);
        table.setWidths(new float[] { 3f, 1.5f, 4f });

        // Headers
        addTableHeader(table, "Suspected Condition");
        addTableHeader(table, "Confidence");
        addTableHeader(table, "Matched Parameters");

        for (int i = 0; i < Math.min(matches.size(), 5); i++) {
            DiseaseMatch match = matches.get(i);

            // Disease name
            Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            PdfPCell nameCell = new PdfPCell(new Phrase(match.getDisease().getName(), nameFont));
            nameCell.setPadding(6);
            nameCell.setBorderColor(Color.LIGHT_GRAY);
            table.addCell(nameCell);

            // Confidence with color coding
            Color confColor = match.getConfidence() >= 80 ? new Color(244, 67, 54)
                    : match.getConfidence() >= 60 ? new Color(255, 152, 0) : new Color(76, 175, 80);
            Font confFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, confColor);
            PdfPCell confCell = new PdfPCell(new Phrase(String.format("%.0f%%", match.getConfidence()), confFont));
            confCell.setPadding(6);
            confCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            confCell.setBorderColor(Color.LIGHT_GRAY);
            table.addCell(confCell);

            // Matched parameters
            Font paramFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            PdfPCell paramCell = new PdfPCell(new Phrase(String.join(", ", match.getMatchedParameters()), paramFont));
            paramCell.setPadding(6);
            paramCell.setBorderColor(Color.LIGHT_GRAY);
            table.addCell(paramCell);
        }

        document.add(table);

        // Add possible causes for top match
        if (!matches.isEmpty()) {
            DiseaseMatch topMatch = matches.get(0);
            Font causesFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.DARK_GRAY);
            Paragraph causes = new Paragraph(
                    "Possible causes for " + topMatch.getDisease().getName() + ": " +
                            topMatch.getDisease().getPossibleCauses(),
                    causesFont);
            causes.setSpacingAfter(10f);
            document.add(causes);
        }
    }

    private void addBulletList(Document document, List<String> items, Color bulletColor) throws DocumentException {
        Font itemFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font bulletFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, bulletColor);

        for (String item : items) {
            Paragraph p = new Paragraph();
            p.add(new Chunk("• ", bulletFont));
            p.add(new Chunk(item, itemFont));
            p.setIndentationLeft(15f);
            document.add(p);
        }
    }

    private void addNumberedList(Document document, List<String> items, Color numberColor) throws DocumentException {
        Font itemFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font numberFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, numberColor);

        for (int i = 0; i < items.size(); i++) {
            Paragraph p = new Paragraph();
            p.add(new Chunk((i + 1) + ". ", numberFont));
            p.add(new Chunk(items.get(i), itemFont));
            p.setIndentationLeft(15f);
            p.setSpacingAfter(3f);
            document.add(p);
        }
    }

    private void addVitalsTable(Document document, List<VitalReadingResponse> recentVitals) throws DocumentException {
        PdfPTable vitalsTable = new PdfPTable(4);
        vitalsTable.setWidthPercentage(100);
        vitalsTable.setSpacingAfter(15f);

        // Header
        addTableHeader(vitalsTable, "Time");
        addTableHeader(vitalsTable, "Heart Rate (bpm)");
        addTableHeader(vitalsTable, "SpO2 (%)");
        addTableHeader(vitalsTable, "Temp (°C)");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        for (VitalReadingResponse v : recentVitals) {
            addVitalCell(vitalsTable, v.getRecordedAt().format(dtf), false);
            addVitalCell(vitalsTable, String.valueOf(v.getHeartRate()),
                    v.getHeartRate() < 60 || v.getHeartRate() > 100);
            addVitalCell(vitalsTable, String.valueOf(v.getSpo2()), v.getSpo2() < 95);
            addVitalCell(vitalsTable, String.valueOf(v.getTemperature()), v.getTemperature() >= 38.0);
        }

        if (recentVitals.isEmpty()) {
            PdfPCell cell = new PdfPCell(new Phrase("No recent vital readings recorded"));
            cell.setColspan(4);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(10);
            vitalsTable.addCell(cell);
        }

        document.add(vitalsTable);
    }

    private void addVitalCell(PdfPTable table, String value, boolean isAbnormal) {
        Font font;
        if (isAbnormal) {
            font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(244, 67, 54));
        } else {
            font = FontFactory.getFont(FontFactory.HELVETICA, 10);
        }
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        cell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(
                new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        cell.setBackgroundColor(new Color(66, 66, 66));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        table.addCell(cell);
    }

    private void addFooter(Document document, boolean hasUrgent) throws DocumentException {
        document.add(new Paragraph("\n\n"));

        // Warning if urgent
        if (hasUrgent) {
            Font warningFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(244, 67, 54));
            Paragraph warning = new Paragraph(
                    "⚠ ATTENTION: Vital signs indicate possible serious condition. Immediate medical evaluation recommended.",
                    warningFont);
            warning.setAlignment(Element.ALIGN_CENTER);
            warning.setSpacingAfter(10f);
            document.add(warning);
        }

        // Disclaimer
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);
        Paragraph footer = new Paragraph(
                "DISCLAIMER: This report is generated automatically by the Medico IoT Health Monitoring System. " +
                        "Suspected conditions are based on vital sign patterns and should NOT be used as a definitive diagnosis. "
                        +
                        "Please consult with a qualified healthcare professional for proper medical evaluation and treatment.",
                footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        // Lab tests note
        Font labFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
        Paragraph labNote = new Paragraph(
                "\nNote: Conditions like Malaria, Dengue, Typhoid, and Hepatitis require laboratory tests for confirmation.",
                labFont);
        labNote.setAlignment(Element.ALIGN_CENTER);
        document.add(labNote);
    }

    private void addInfoCell(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(4);
        table.addCell(labelCell);

        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "N/A", valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(4);
        table.addCell(valueCell);
    }
}
