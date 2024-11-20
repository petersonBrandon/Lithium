package com.lithium.util.reporter.generation;

import com.lithium.cli.util.ResultType;
import com.lithium.cli.util.TestResult;
import com.lithium.util.logger.LithiumLogger;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

public class PdfReporter extends BaseReporter {
    private static final LithiumLogger log = LithiumLogger.getInstance();

    private static final BaseColor PRIMARY_COLOR = new BaseColor(59, 130, 246);    // Modern blue
    private static final BaseColor SUCCESS_COLOR = new BaseColor(34, 197, 94);     // Modern green
    private static final BaseColor ERROR_COLOR = new BaseColor(239, 68, 68);       // Modern red
    private static final BaseColor LIGHT_GRAY = new BaseColor(248, 250, 252);      // Softer gray
    private static final BaseColor BORDER_COLOR = new BaseColor(226, 232, 240);    // Subtle border
    private static final float SPACING = 40f;

    public PdfReporter(String executionId, String projectName, String reportPath, String testSource) {
        super(executionId, projectName, reportPath, testSource);
    }

    @Override
    public void generateReport(List<TestResult> testResults) {
        try {
            String fileName = reportPath + "/report.pdf";
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();
            addContent(document, testResults);
            document.close();

            log.success("PDF report generated: " + fileName);
        } catch (Exception e) {
            log.error("Failed to generate PDF report: " + e.getMessage());
        }
    }

    private void addContent(Document document, List<TestResult> testResults) throws DocumentException {
        TestExecutionStats stats = calculateStats(testResults);

        // Modern Header
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 28, Font.NORMAL, PRIMARY_COLOR);
        Paragraph title = new Paragraph(String.format("%s Execution Report", testSource), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(15);
        document.add(title);

        // Subtitle with 12-hour time format
        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 13, Font.NORMAL, BaseColor.GRAY);
        Paragraph subtitle = new Paragraph(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a")), subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(SPACING);
        document.add(subtitle);

        // Stats Grid with modern styling
        PdfPTable statsTable = new PdfPTable(5);
        statsTable.setWidthPercentage(100);
        statsTable.setSpacingAfter(SPACING);
        statsTable.setSpacingBefore(SPACING/2);

        // Convert duration to seconds with decimal point
        String duration = String.format("%.1f s", stats.totalDuration().toMillis() / 1000.0);

        addStatCell(statsTable, "Total Tests", String.valueOf(stats.totalTests()), PRIMARY_COLOR);
        addStatCell(statsTable, "Passed Tests", String.valueOf(stats.passedTests()), SUCCESS_COLOR);
        addStatCell(statsTable, "Failed Tests", String.valueOf(stats.failedTests()), ERROR_COLOR);
        addStatCell(statsTable, "Success Rate", String.format("%.1f%%", stats.successRate()), PRIMARY_COLOR);
        addStatCell(statsTable, "Duration", duration, PRIMARY_COLOR);

        document.add(statsTable);

        // Results Table with modern styling
        PdfPTable resultsTable = new PdfPTable(4);
        resultsTable.setWidthPercentage(100);
        resultsTable.setSpacingBefore(SPACING/2);
        float[] columnWidths = {3.5f, 2, 2, 3.5f};
        resultsTable.setWidths(columnWidths);

        // Modern table headers
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(71, 85, 105));
        Stream.of("Test Name", "Status", "Duration", "Error")
                .forEach(headerText -> {
                    PdfPCell header = new PdfPCell(new Phrase(headerText, headerFont));
                    header.setBackgroundColor(LIGHT_GRAY);
                    header.setPadding(12);
                    header.setBorderColor(BORDER_COLOR);
                    header.setBorderWidth(1);
                    resultsTable.addCell(header);
                });

        // Modern table content
        Font contentFont = new Font(Font.FontFamily.HELVETICA, 11);
        for (TestResult result : testResults) {
            Duration d = Duration.between(result.startTime(), result.endTime());
            String testDuration = String.format("%.2f s", d.toMillis() / 1000.0);

            // Test name cell
            PdfPCell nameCell = new PdfPCell(new Phrase(result.testName(), contentFont));
            nameCell.setPadding(10);
            nameCell.setBorderColor(BORDER_COLOR);
            resultsTable.addCell(nameCell);

            // Status cell with modern pill-style background
            PdfPCell statusCell = new PdfPCell();
            BaseColor statusColor = result.result() == ResultType.PASS ? SUCCESS_COLOR : ERROR_COLOR;
            Font statusFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
            Paragraph statusPara = new Paragraph(result.result().toString(), statusFont);
            statusCell.setBackgroundColor(statusColor);
            statusCell.addElement(statusPara);
            statusCell.setPadding(8);
            statusCell.setBorderColor(BORDER_COLOR);
            resultsTable.addCell(statusCell);

            // Duration cell
            PdfPCell durationCell = new PdfPCell(new Phrase(testDuration, contentFont));
            durationCell.setPadding(10);
            durationCell.setBorderColor(BORDER_COLOR);
            resultsTable.addCell(durationCell);

            // Error message cell
            String errorMessage = result.errorMessage() != null ? result.errorMessage() : "";
            Font errorFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, ERROR_COLOR);
            PdfPCell errorCell = new PdfPCell(new Phrase(errorMessage, errorFont));
            errorCell.setPadding(10);
            errorCell.setBorderColor(BORDER_COLOR);
            resultsTable.addCell(errorCell);
        }

        document.add(resultsTable);
    }

    private void addStatCell(PdfPTable table, String label, String value, BaseColor color) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(15);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(LIGHT_GRAY);

        Font labelFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, new BaseColor(71, 85, 105));
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, color);

        Paragraph labelPara = new Paragraph(label, labelFont);
        labelPara.setSpacingAfter(5);
        Paragraph valuePara = new Paragraph(value, valueFont);

        cell.addElement(labelPara);
        cell.addElement(valuePara);

        table.addCell(cell);
    }
}
