package com.lithium.cli.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TestExecutionSummary {
    private final List<TestResult> testResults = new ArrayList<>();
    private final TerminalOutput output;

    public TestExecutionSummary(TerminalOutput output) {
        this.output = output;
    }

    public void addResult(TestResult result) {
        testResults.add(result);
    }

    public void printSummary() {
        int totalTests = testResults.size();
        int passedTests = (int) testResults.stream()
                .filter(r -> r.result() == ResultType.PASS)
                .count();
        int failedTests = totalTests - passedTests;
        double successRate = totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0;

        output.printSeparator(true);
        output.printInfo("                  TEST EXECUTION SUMMARY                     ");
        output.printSeparator(true);

        Duration totalDuration = Duration.between(
                testResults.getFirst().startTime(),
                testResults.getLast().endTime()
        );

        output.printInfo("");
        output.printInfo("Total Duration: " + totalDuration.toSeconds() + " seconds");
        output.printInfo("Total Tests: " + totalTests);
        output.printSuccess("Passed Tests: " + passedTests + " ✓");
        output.printError("Failed Tests: " + failedTests + " ✗");
        output.printInfo("Success Rate: " + String.format("%.2f", successRate) + "%");
        output.printInfo("");

        if (failedTests > 0) {
            printFailedTestDetails();
        }

        output.printSeparator(true);
    }

    private void printFailedTestDetails() {
        output.printSeparator(false);
        output.printError("FAILED TESTS DETAILS");
        output.printSeparator(false);

        testResults.stream()
                .filter(r -> r.result() == ResultType.FAIL)
                .forEach(r -> {
                    output.printError("Test Name: " + r.testName());
                    output.printError("File: " + r.fileName());
                    output.printError("Error: " + r.errorMessage());
                    output.printError("Duration: " +
                            Duration.between(r.startTime(), r.endTime()).toMillis() + " ms");
                    output.printInfo("");
                });
    }
}