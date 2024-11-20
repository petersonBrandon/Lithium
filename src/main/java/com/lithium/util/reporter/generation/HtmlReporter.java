package com.lithium.util.reporter.generation;

import com.lithium.cli.util.ResultType;
import com.lithium.cli.util.TestResult;
import com.lithium.util.logger.LithiumLogger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HtmlReporter extends BaseReporter {
    private static final LithiumLogger log = LithiumLogger.getInstance();

    public HtmlReporter(String executionId, String projectName, String reportPath, String testSource) {
        super(executionId, projectName, reportPath, testSource);
    }

    @Override
    public void generateReport(List<TestResult> testResults) {
        try {
            String fileName = reportPath + "/report.html";
            Files.writeString(Paths.get(fileName), generateHtmlContent(testResults));
            log.success("HTML report generated: " + fileName);
        } catch (Exception e) {
            log.error("Failed to generate HTML report: " + e.getMessage());
        }
    }

    private String generateHtmlContent(List<TestResult> testResults) {
        TestExecutionStats stats = calculateStats(testResults);
        StringBuilder html = new StringBuilder();

        html.append("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Test Execution Report</title>
                <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
                <style>
                    :root {
                        --primary-color: #2563eb;
                        --success-color: #16a34a;
                        --error-color: #dc2626;
                        --background-color: #f8fafc;
                        --card-background: #ffffff;
                    }
                    
                    body {
                        font-family: 'Inter', sans-serif;
                        line-height: 1.5;
                        margin: 0;
                        padding: 2rem;
                        background-color: var(--background-color);
                        color: #1e293b;
                    }
                    
                    .container {
                        max-width: 1200px;
                        margin: 0 auto;
                    }
                    
                    .header {
                        text-align: center;
                        margin-bottom: 2rem;
                    }
                    
                    .stats-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
                        gap: 1rem;
                        margin-bottom: 2rem;
                    }
                    
                    .stat-card {
                        background: var(--card-background);
                        padding: 1.5rem;
                        border-radius: 0.5rem;
                        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
                    }
                    
                    .stat-card h3 {
                        margin: 0;
                        font-size: 0.875rem;
                        color: #64748b;
                    }
                    
                    .stat-card p {
                        margin: 0.5rem 0 0;
                        font-size: 1.5rem;
                        font-weight: 600;
                    }
                    
                    .results-table {
                        width: 100%;
                        background: var(--card-background);
                        border-radius: 0.5rem;
                        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                    }
                    
                    .results-table th {
                        background: #f1f5f9;
                        font-weight: 500;
                        text-align: left;
                        padding: 1rem;
                    }
                    
                    .results-table td {
                        padding: 1rem;
                        border-top: 1px solid #e2e8f0;
                    }
                    
                    .status {
                        display: inline-flex;
                        align-items: center;
                        padding: 0.25rem 0.75rem;
                        border-radius: 9999px;
                        font-weight: 500;
                        font-size: 0.875rem;
                    }
                    
                    .status.passed {
                        background: #dcfce7;
                        color: var(--success-color);
                    }
                    
                    .status.failed {
                        background: #fee2e2;
                        color: var(--error-color);
                    }
                    
                    .error-message {
                        color: var(--error-color);
                        font-size: 0.875rem;
                    }
                </style>
            </head>
            <body>
                <div class="container">
            """);

        // Header
        html.append("""
            <div class="header">
                <h1>%s Execution Report</h1>
                <p>%s</p>
            </div>
            """.formatted(testSource, LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a"))));

        // Stats Grid
        html.append("""
            <div class="stats-grid">
                <div class="stat-card">
                    <h3>Total Tests</h3>
                    <p>%d</p>
                </div>
                <div class="stat-card">
                    <h3>Passed Tests</h3>
                    <p style="color: var(--success-color)">%d</p>
                </div>
                <div class="stat-card">
                    <h3>Failed Tests</h3>
                    <p style="color: var(--error-color)">%d</p>
                </div>
                <div class="stat-card">
                    <h3>Success Rate</h3>
                    <p>%.1f%%</p>
                </div>
                <div class="stat-card">
                    <h3>Total Duration</h3>
                    <p>%ds</p>
                </div>
            </div>
            """.formatted(
                stats.totalTests(),
                stats.passedTests(),
                stats.failedTests(),
                stats.successRate(),
                stats.totalDuration().toSeconds()
        ));

        // Results Table
        html.append("""
            <table class="results-table">
                <thead>
                    <tr>
                        <th>Test Name</th>
                        <th>Status</th>
                        <th>Duration</th>
                        <th>Error</th>
                    </tr>
                </thead>
                <tbody>
            """);

        for (TestResult result : testResults) {
            Duration duration = Duration.between(result.startTime(), result.endTime());
            String status = result.result() == ResultType.PASS ? "passed" : "failed";

            html.append("""
                <tr>
                    <td>%s</td>
                    <td><span class="status %s">%s</span></td>
                    <td>%d s</td>
                    <td class="error-message">%s</td>
                </tr>
                """.formatted(
                    result.testName(),
                    status,
                    result.result(),
                    duration.toSeconds(),
                    result.errorMessage() != null ? result.errorMessage() : ""
            ));
        }

        html.append("""
                    </tbody>
                </table>
            </div>
        </body>
        </html>
        """);

        return html.toString();
    }
}
