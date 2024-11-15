/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: RunCommand.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.cli.commands;

import com.lithium.cli.BaseLithiumCommand;
import com.lithium.core.TestCase;
import com.lithium.core.TestRunner;
import com.lithium.exceptions.TestSyntaxException;
import com.lithium.parser.TestParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Run Command allows for the running of .lit tests.
 */
public class RunCommand extends BaseLithiumCommand {
    private static final Logger log = LogManager.getLogger(RunCommand.class);
    private static final List<RunCommand.TestResult> testResults = new ArrayList<>();
    private static final String SEPARATOR = "════════════════════════════════════════════════════════════";
    private static final String SUB_SEPARATOR = "────────────────────────────────────────────────────────";

    /**
     * Get the description of the 'run' command
     *
     * @return Run Command description
     */
    @Override
    public String getDescription() {
        return "Executes Lithium test files";
    }

    /**
     * Get the usage details of the 'run' command
     *
     * @return Usage Details
     */
    @Override
    public String getUsage() {
        return "lit run <file-name> [test-name] [--headed] [--maximized]";
    }

    /**
     * Main execution command of the 'run' command
     * @param args command args
     */
    @Override
    public void execute(String[] args) {
        validateArgsLength(args, 2);

        boolean headless = !Arrays.asList(args).contains("--headed");
        boolean maximized = Arrays.asList(args).contains("--maximized");
        TestRunner runner = null;
        String fileName = args[1];

        try {
            String testFilePath = System.getProperty("user.dir") + "\\" + fileName + ".lit";

            File testFile = new File(testFilePath);
            if (!testFile.exists()) {
                throw new FileNotFoundException("Test file '" + testFilePath + "' not found!");
            }

            TestParser parser = new TestParser();
            Map<String, TestCase> testCases = parser.parseFile(testFilePath);
            runner = new TestRunner(headless, maximized);

            if (args.length > 2 && !args[2].startsWith("--")) {
                String testName = args[2];
                TestCase test = testCases.get(testName);
                if (test == null) {
                    throw new IllegalArgumentException("Test '" + testName + "' not found!");
                }
                runAndLogTest(runner, test, fileName);
            } else {
                for (TestCase test : testCases.values()) {
                    runAndLogTest(runner, test, fileName);
                }
            }

            // Log summary after all tests are complete
            logTestSummary();

        } catch (TestSyntaxException e) {
            log.fatal("Syntax error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            log.fatal("Error: " + e.getMessage());
            System.exit(1);
        } finally {
            if (runner != null) {
                runner.close();
            }
        }
    }

    /**
     * Execute a test and log the results
     *
     * @param runner test runner instance
     * @param test test case to be run
     * @param fileName test file name
     */
    private static void runAndLogTest(TestRunner runner, TestCase test, String fileName) {
        LocalDateTime startTime = LocalDateTime.now();
        String errorMessage = null;
        RunCommand.ResultType result;

        log.info(SUB_SEPARATOR);

        try {
            runner.runTest(test);
            result = RunCommand.ResultType.PASS;
            log.info("Status: ✓ PASSED");
        } catch (Exception e) {
            result = RunCommand.ResultType.FAIL;
            errorMessage = e.getMessage();
            log.error("Status: ✗ FAILED");
            log.error("Error: {}", errorMessage);
        }

        Duration duration = Duration.between(startTime, LocalDateTime.now());
        log.info("Duration: {} ms", duration.toMillis());

        testResults.add(new RunCommand.TestResult(
                fileName,
                test.getName(),
                result,
                startTime,
                LocalDateTime.now(),
                errorMessage
        ));
    }

    /**
     * Output summary of test executions
     */
    private static void logTestSummary() {
        int totalTests = testResults.size();
        int passedTests = (int) testResults.stream()
                .filter(r -> r.result == RunCommand.ResultType.PASS)
                .count();
        int failedTests = totalTests - passedTests;
        double successRate = totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0;

        log.info(SEPARATOR);
        log.info("                     TEST EXECUTION SUMMARY                     ");
        log.info(SEPARATOR);

        // Calculate duration for entire test suite
        Duration totalDuration = Duration.between(
                testResults.get(0).startTime,
                testResults.get(testResults.size() - 1).endTime
        );

        // Summary Statistics
        log.info("");
        log.info("Total Duration: {} seconds", totalDuration.toSeconds());
        log.info("Total Tests: {}", totalTests);
        log.info("Passed Tests: {} ✓", passedTests);
        log.info("Failed Tests: {} ✗", failedTests);
        log.info("Success Rate: {}%", successRate);
        log.info("");

        // Failed Tests Details
        if (failedTests > 0) {
            log.info(SUB_SEPARATOR);
            log.info("FAILED TESTS DETAILS");
            log.info(SUB_SEPARATOR);

            testResults.stream()
                    .filter(r -> r.result == RunCommand.ResultType.FAIL)
                    .forEach(r -> {
                        log.error("Test Name: {}", r.testName);
                        log.error("File: {}", r.fileName);
                        log.error("Error: {}", r.errorMessage);
                        log.error("Duration: {} ms",
                                Duration.between(r.startTime, r.endTime).toMillis());
                    });
        }

        log.info(SEPARATOR);
    }

    /**
     * Test result types
     */
    public enum ResultType {
        PASS,
        FAIL,
        SKIP
    }

    /**
     * The TestResult class handles all test result data
     */
    private static class TestResult {
        final String fileName;
        final String testName;
        final RunCommand.ResultType result;
        final LocalDateTime startTime;
        final LocalDateTime endTime;
        final String errorMessage;

        /**
         * Construct the test result
         *
         * @param fileName What the test file name is.
         * @param testName What the test name is.
         * @param result What the test result is.
         * @param startTime What the test start time is.
         * @param endTime What the test end time is.
         * @param errorMessage What is the error message is.
         */
        TestResult(String fileName, String testName, RunCommand.ResultType result,
                   LocalDateTime startTime, LocalDateTime endTime, String errorMessage) {
            this.fileName = fileName;
            this.testName = testName;
            this.result = result;
            this.startTime = startTime;
            this.endTime = endTime;
            this.errorMessage = errorMessage;
        }
    }
}
