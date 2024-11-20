package com.lithium.util.reporter.generation;

import com.lithium.cli.util.ResultType;
import com.lithium.cli.util.TestResult;
import java.time.Duration;
import java.util.List;

public abstract class BaseReporter {
    protected final String executionId;
    protected final String projectName;
    protected final String reportPath;
    protected final String testSource;

    protected BaseReporter(String executionId, String projectName, String reportPath, String testSource) {
        this.executionId = executionId;
        this.projectName = projectName;
        this.reportPath = reportPath;
        this.testSource = testSource;
    }

    public abstract void generateReport(List<TestResult> testResults);

    protected TestExecutionStats calculateStats(List<TestResult> testResults) {
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

    protected record TestExecutionStats(
            int totalTests,
            int passedTests,
            int failedTests,
            double successRate,
            Duration totalDuration
    ) {}
}
