package com.lithium.util.reporter;

import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.lithium.cli.util.ResultType;
import com.lithium.cli.util.TestResult;
import com.lithium.util.logger.LithiumLogger;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class LithiumReporter {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private final String reportDirectory;
    private final List<String> reportFormats;
    private final String projectName;
    private final String executionId;

    public LithiumReporter(String reportDirectory, List<String> reportFormats, String projectName) {
        this.reportDirectory = reportDirectory;
        this.reportFormats = reportFormats;
        this.projectName = projectName;
        this.executionId = generateExecutionId();
    }

    public void generateReports(List<TestResult> testResults) {
        createReportDirectory();

        for (String format : reportFormats) {
            try {
                switch (format.toLowerCase()) {
                    case "html" -> generateHtmlReport(testResults);
                    case "pdf" -> generatePdfReport(testResults);
                    default -> log.warn("Unsupported report format: " + format);
                }
            } catch (Exception e) {
                log.error("Failed to generate " + format + " report: " + e.getMessage());
            }
        }
    }

    private void createReportDirectory() {
        File directory = new File(reportDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    private String generateExecutionId() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    private void generateHtmlReport(List<TestResult> testResults) throws IOException {
        String fileName = String.format("%s/report_%s.html", reportDirectory, executionId);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(generateHtmlContent(testResults));
        }
        log.success("HTML report generated: " + fileName);
    }

    private void generatePdfReport(List<TestResult> testResults) throws IOException, DocumentException {
        String fileName = String.format("%s/report_%s.pdf", reportDirectory, executionId);
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(fileName));

        document.open();
        addPdfContent(document, testResults);
        document.close();

        log.success("PDF report generated: " + fileName);
    }

    private Map<String, Object> createReportData(List<TestResult> testResults) {
        TestExecutionStats stats = calculateStats(testResults);

        return Map.of(
                "projectName", projectName,
                "executionId", executionId,
                "executionDate", LocalDateTime.now().toString(),
                "summary", Map.of(
                        "totalTests", stats.totalTests,
                        "passedTests", stats.passedTests,
                        "failedTests", stats.failedTests,
                        "successRate", stats.successRate,
                        "totalDuration", stats.totalDuration.toSeconds()
                ),
                "testResults", testResults
        );
    }

    private String generateHtmlContent(List<TestResult> testResults) {
        TestExecutionStats stats = calculateStats(testResults);
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html><html><head><title>Test Execution Report</title>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; margin: 20px; }")
                .append(".passed { color: green; } .failed { color: red; }")
                .append("table { border-collapse: collapse; width: 100%; }")
                .append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }")
                .append("</style></head><body>")
                .append("<h1>Test Execution Report</h1>")
                .append("<h2>Summary</h2>")
                .append("<p>Project: ").append(projectName).append("</p>")
                .append("<p>Execution ID: ").append(executionId).append("</p>")
                .append("<p>Total Tests: ").append(stats.totalTests).append("</p>")
                .append("<p>Passed Tests: ").append(stats.passedTests).append("</p>")
                .append("<p>Failed Tests: ").append(stats.failedTests).append("</p>")
                .append("<p>Success Rate: ").append(String.format("%.2f%%", stats.successRate)).append("</p>")
                .append("<p>Total Duration: ").append(stats.totalDuration.toSeconds()).append(" seconds</p>");

        // Add test results table
        html.append("<h2>Test Results</h2><table>")
                .append("<tr><th>Test Name</th><th>Status</th><th>Duration</th><th>Error</th></tr>");

        for (TestResult result : testResults) {
            Duration duration = Duration.between(result.startTime(), result.endTime());
            html.append("<tr>")
                    .append("<td>").append(result.testName()).append("</td>")
                    .append("<td class='").append(result.result().toString().toLowerCase()).append("'>")
                    .append(result.result()).append("</td>")
                    .append("<td>").append(duration.toMillis()).append(" ms</td>")
                    .append("<td>").append(result.errorMessage() != null ? result.errorMessage() : "").append("</td>")
                    .append("</tr>");
        }

        html.append("</table></body></html>");
        return html.toString();
    }

    private void addPdfContent(Document document, List<TestResult> testResults) throws DocumentException {
        TestExecutionStats stats = calculateStats(testResults);

        // Add title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Test Execution Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        // Add summary
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);
        document.add(new Paragraph("Project: " + projectName, normalFont));
        document.add(new Paragraph("Execution ID: " + executionId, normalFont));
        document.add(new Paragraph("Total Tests: " + stats.totalTests, normalFont));
        document.add(new Paragraph("Passed Tests: " + stats.passedTests, normalFont));
        document.add(new Paragraph("Failed Tests: " + stats.failedTests, normalFont));
        document.add(new Paragraph("Success Rate: " + String.format("%.2f%%", stats.successRate), normalFont));
        document.add(new Paragraph("Total Duration: " + stats.totalDuration.toSeconds() + " seconds", normalFont));
        document.add(Chunk.NEWLINE);

        // Add test results table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);

        // Add table headers
        Stream.of("Test Name", "Status", "Duration", "Error")
                .forEach(header -> {
                    PdfPCell cell = new PdfPCell(new Phrase(header, normalFont));
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    table.addCell(cell);
                });

        // Add test results
        for (TestResult result : testResults) {
            Duration duration = Duration.between(result.startTime(), result.endTime());
            table.addCell(result.testName());
            table.addCell(result.result().toString());
            table.addCell(duration.toMillis() + " ms");
            table.addCell(result.errorMessage() != null ? result.errorMessage() : "");
        }

        document.add(table);
    }

    private record TestExecutionStats(
            int totalTests,
            int passedTests,
            int failedTests,
            double successRate,
            Duration totalDuration
    ) {}

    private TestExecutionStats calculateStats(List<TestResult> testResults) {
        int totalTests = testResults.size();
        int passedTests = (int) testResults.stream()
                .filter(r -> r.result() == ResultType.PASS)
                .count();
        int failedTests = totalTests - passedTests;
        double successRate = totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0;

        Duration totalDuration = Duration.between(
                testResults.getFirst().startTime(),
                testResults.getLast().endTime()
        );

        return new TestExecutionStats(totalTests, passedTests, failedTests, successRate, totalDuration);
    }
}
