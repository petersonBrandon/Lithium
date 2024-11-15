/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: LithiumInterpreter.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium;

import com.lithium.core.TestCase;
import com.lithium.core.TestRunner;
import com.lithium.exceptions.TestSyntaxException;
import com.lithium.parser.TestParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.*;
import java.time.LocalDateTime;

/**
 * The LithiumInterpreter class is responsible for executing Lithium test files (.lit).
 * It parses the specified file and runs test cases either in headless or maximized mode,
 * depending on the arguments provided.
 */
public class LithiumInterpreter {
    private static final Logger log = LogManager.getLogger(LithiumInterpreter.class);
    private static final List<TestResult> testResults = new ArrayList<>();
    private static final String SEPARATOR = "════════════════════════════════════════════════════════════";
    private static final String SUB_SEPARATOR = "────────────────────────────────────────────────────────";

    /**
     * Main method that serves as the entry point for running Lithium test files.
     *
     * @param args Command-line arguments specifying the operation and options for running tests.
     *             Usage: lit run <file-name> [test-name] [--headed] [--maximized]
     *             <ul>
     *               <li><code>run</code>: Command to execute a test file.</li>
     *               <li><code>file-name</code>: Name of the test file without the ".lit" extension.</li>
     *               <li><code>test-name</code> (optional): Specific test to run within the file.</li>
     *               <li><code>--headed</code> (optional): Run tests in headed mode.</li>
     *               <li><code>--maximized</code> (optional): Run tests in maximized window mode.</li>
     *             </ul>
     */
    public static void main(String[] args) {
        if (args.length < 2 || !args[0].equals("run")) {
            System.out.println("Usage: lit run <file-name> [test-name] [--headed] [--maximized]");
            return;
        }

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

    private static void runAndLogTest(TestRunner runner, TestCase test, String fileName) {
        LocalDateTime startTime = LocalDateTime.now();
        String errorMessage = null;
        ResultType result;

        log.info(SUB_SEPARATOR);

        try {
            runner.runTest(test);
            result = ResultType.PASS;
            log.info("Status: ✓ PASSED");
        } catch (Exception e) {
            result = ResultType.FAIL;
            errorMessage = e.getMessage();
            log.error("Status: ✗ FAILED");
            log.error("Error: {}", errorMessage);
        }

        Duration duration = Duration.between(startTime, LocalDateTime.now());
        log.info("Duration: {} ms", duration.toMillis());

        testResults.add(new TestResult(
                fileName,
                test.getName(),
                result,
                startTime,
                LocalDateTime.now(),
                errorMessage
        ));
    }

    private static void logTestSummary() {
        int totalTests = testResults.size();
        int passedTests = (int) testResults.stream()
                .filter(r -> r.result == ResultType.PASS)
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
                    .filter(r -> r.result == ResultType.FAIL)
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

    public enum ResultType {
        PASS,
        FAIL,
        SKIP
    }

    private static class TestResult {
        final String fileName;
        final String testName;
        final ResultType result;
        final LocalDateTime startTime;
        final LocalDateTime endTime;
        final String errorMessage;

        TestResult(String fileName, String testName, ResultType result,
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