package com.lithium.cli.util;

import com.lithium.util.logger.LithiumLogger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TestExecutionLogger {
    private final List<TestResult> testResults = new ArrayList<>();
    private static final LithiumLogger log = LithiumLogger.getInstance();

    public void addResult(TestResult result) {
        testResults.add(result);
    }

    public List<TestResult> getResults() {
        return testResults;
    }

    public void printSummary() {
        int totalTests = testResults.size();
        int passedTests = (int) testResults.stream()
                .filter(r -> r.result() == ResultType.PASS)
                .count();
        int failedTests = totalTests - passedTests;
        double successRate = totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0;

        log.printSeparator(true);
        log.basic("                  TEST EXECUTION SUMMARY");
        log.printSeparator(true);

        Duration totalDuration = Duration.between(
                testResults.getFirst().startTime(),
                testResults.getLast().endTime()
        );

        log.basic("");
        log.basic("Total Duration: " + totalDuration.toSeconds() + " seconds");
        log.basic("Total Tests: " + totalTests);
        log.success("Passed Tests: " + passedTests + " ✓");
        log.fail("Failed Tests: " + failedTests + " ✗");
        log.basic("Success Rate: " + String.format("%.2f", successRate) + "%");
        log.basic("");

        if (failedTests > 0) {
            printFailedTestDetails();
        }

        log.printSeparator(true);
    }

    private void printFailedTestDetails() {
        log.printSeparator(false);
        log.fail("FAILED TESTS DETAILS");
        log.printSeparator(false);

        testResults.stream()
                .filter(r -> r.result() == ResultType.FAIL)
                .forEach(r -> {
                    log.fail("Test Name: " + r.testName());
                    log.fail("File: " + r.fileName());
                    log.fail("Error: " + r.errorMessage());
                    log.fail("Duration: " +
                            Duration.between(r.startTime(), r.endTime()).toMillis() + " ms");
                    log.basic("");
                });
    }
}