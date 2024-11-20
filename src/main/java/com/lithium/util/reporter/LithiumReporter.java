package com.lithium.util.reporter;

import com.lithium.cli.util.TestResult;
import com.lithium.util.logger.LithiumLogger;
import com.lithium.util.reporter.generation.BaseReporter;
import com.lithium.util.reporter.generation.HtmlReporter;
import com.lithium.util.reporter.generation.PdfReporter;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LithiumReporter {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private final String baseReportDirectory;
    private final List<String> reportFormats;
    private final String projectName;
    private final String executionId;
    private final String testSource;

    public LithiumReporter(String reportDirectory, List<String> reportFormats, String projectName, String testSource) {
        this.baseReportDirectory = reportDirectory;
        this.reportFormats = reportFormats;
        this.projectName = projectName;
        this.testSource = testSource;
        this.executionId = generateExecutionId();
    }

    public void generateReports(List<TestResult> testResults) {
        String reportPath = createReportDirectory();

        for (String format : reportFormats) {
            try {
                BaseReporter reporter = switch (format.toLowerCase()) {
                    case "html" -> new HtmlReporter(executionId, projectName, reportPath, testSource);
                    case "pdf" -> new PdfReporter(executionId, projectName, reportPath, testSource);
                    default -> {
                        log.warn("Unsupported report format: " + format);
                        yield null;
                    }
                };

                if (reporter != null) {
                    reporter.generateReport(testResults);
                }
            } catch (Exception e) {
                log.error("Failed to generate " + format + " report: " + e.getMessage());
            }
        }
    }

    private String createReportDirectory() {
        // Create base directory if it doesn't exist
        File baseDir = new File(baseReportDirectory);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }

        // Get test source name (file or folder)
        String sourceName = Path.of(testSource).getFileName().toString();
        if (sourceName.endsWith(".lit")) {
            sourceName = sourceName.substring(0, sourceName.length() - 4);
        }

        // Create unique subfolder
        String subfolderName = sourceName;
        File subfolder = new File(baseDir, subfolderName);
        int counter = 1;
        while (subfolder.exists()) {
            subfolderName = sourceName + " (" + counter + ")";
            subfolder = new File(baseDir, subfolderName);
            counter++;
        }

        String reportPath = Paths.get(baseReportDirectory, subfolderName).toString();
        new File(reportPath).mkdirs();
        return reportPath;
    }

    private String generateExecutionId() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}
