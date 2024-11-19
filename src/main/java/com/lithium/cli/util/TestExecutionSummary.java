package com.lithium.cli.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TestExecutionSummary {
    private final List<TestResult> testResults = new ArrayList<>();
    private final LithiumTerminal terminal;

    public TestExecutionSummary() {
        this.terminal = LithiumTerminal.getInstance();
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

        terminal.printSeparator(true);
        terminal.printInfo("             TEST EXECUTION SUMMARY");
        terminal.printSeparator(true);

        Duration totalDuration = Duration.between(
                testResults.getFirst().startTime(),
                testResults.getLast().endTime()
        );

        terminal.printInfo("");
        terminal.printInfo("Total Duration: " + totalDuration.toSeconds() + " seconds");
        terminal.printInfo("Total Tests: " + totalTests);
        terminal.printSuccess("Passed Tests: " + passedTests + " ✓");
        terminal.printError("Failed Tests: " + failedTests + " ✗");
        terminal.printInfo("Success Rate: " + String.format("%.2f", successRate) + "%");
        terminal.printInfo("");

        if (failedTests > 0) {
            printFailedTestDetails();
        }

        terminal.printSeparator(true);
    }

    private void printFailedTestDetails() {
        terminal.printSeparator(false);
        terminal.printError("FAILED TESTS DETAILS");
        terminal.printSeparator(false);

        testResults.stream()
                .filter(r -> r.result() == ResultType.FAIL)
                .forEach(r -> {
                    terminal.printError("Test Name: " + r.testName());
                    terminal.printError("File: " + r.fileName());
                    terminal.printError("Error: " + r.errorMessage());
                    terminal.printError("Duration: " +
                            Duration.between(r.startTime(), r.endTime()).toMillis() + " ms");
                    terminal.printInfo("");
                });
    }
}