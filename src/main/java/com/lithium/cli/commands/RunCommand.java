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
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    private static final List<RunCommand.TestResult> testResults = new ArrayList<>();
    private static final String SEPARATOR = "════════════════════════════════════════════════════════════";
    private static final String SUB_SEPARATOR = "────────────────────────────────────────────────────────";
    private Terminal terminal;
    private LineReader lineReader;

    public RunCommand() {
        try {
            this.terminal = TerminalBuilder.builder()
                    .system(true)
                    .dumb(true)      // Allow dumb terminal as fallback
                    .jansi(true)     // Enable Jansi support
                    .build();

            this.lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .parser(new DefaultParser())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize terminal: " + e.getMessage(), e);
        }
    }

    @Override
    public String getDescription() {
        return "Executes Lithium test files";
    }

    @Override
    public String getUsage() {
        return "lit run <file-name> [test-name] [--headed] [--maximized]";
    }

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
                printError("Test file '" + testFilePath + "' not found!");
                throw new FileNotFoundException("Test file '" + testFilePath + "' not found!");
            }

            TestParser parser = new TestParser();
            Map<String, TestCase> testCases = parser.parseFile(testFilePath);
            runner = new TestRunner(headless, maximized);

            if (args.length > 2 && !args[2].startsWith("--")) {
                String testName = args[2];
                TestCase test = testCases.get(testName);
                if (test == null) {
                    printError("Test '" + testName + "' not found!");
                    throw new IllegalArgumentException("Test '" + testName + "' not found!");
                }
                runAndLogTest(runner, test, fileName);
            } else {
                for (TestCase test : testCases.values()) {
                    runAndLogTest(runner, test, fileName);
                }
            }

            logTestSummary();

        } catch (TestSyntaxException e) {
            printError("Syntax error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            printError("Error: " + e.getMessage());
            System.exit(1);
        } finally {
            if (runner != null) {
                runner.close();
            }
        }
    }

    private void runAndLogTest(TestRunner runner, TestCase test, String fileName) {
        LocalDateTime startTime = LocalDateTime.now();
        String errorMessage = null;
        RunCommand.ResultType result;

        printSeparator(SUB_SEPARATOR);
        printInfo("Running test: " + test.getName());

        try {
            runner.runTest(test);
            result = RunCommand.ResultType.PASS;
            printSuccess("Status: ✓ PASSED");
        } catch (Exception e) {
            result = RunCommand.ResultType.FAIL;
            errorMessage = e.getMessage();
            printError("Status: ✗ FAILED");
            printError("Error: " + errorMessage);
        }

        Duration duration = Duration.between(startTime, LocalDateTime.now());
        printInfo("Duration: " + duration.toMillis() + " ms");

        testResults.add(new RunCommand.TestResult(
                fileName,
                test.getName(),
                result,
                startTime,
                LocalDateTime.now(),
                errorMessage
        ));
    }

    private void logTestSummary() {
        int totalTests = testResults.size();
        int passedTests = (int) testResults.stream()
                .filter(r -> r.result == RunCommand.ResultType.PASS)
                .count();
        int failedTests = totalTests - passedTests;
        double successRate = totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0;

        printSeparator(SEPARATOR);
        printInfo("                  TEST EXECUTION SUMMARY                     ");
        printSeparator(SEPARATOR);

        Duration totalDuration = Duration.between(
                testResults.get(0).startTime,
                testResults.get(testResults.size() - 1).endTime
        );

        printInfo("");
        printInfo("Total Duration: " + totalDuration.toSeconds() + " seconds");
        printInfo("Total Tests: " + totalTests);
        printSuccess("Passed Tests: " + passedTests + " ✓");
        printError("Failed Tests: " + failedTests + " ✗");
        printInfo("Success Rate: " + String.format("%.2f", successRate) + "%");
        printInfo("");

        if (failedTests > 0) {
            printSeparator(SUB_SEPARATOR);
            printError("FAILED TESTS DETAILS");
            printSeparator(SUB_SEPARATOR);

            testResults.stream()
                    .filter(r -> r.result == RunCommand.ResultType.FAIL)
                    .forEach(r -> {
                        printError("Test Name: " + r.testName);
                        printError("File: " + r.fileName);
                        printError("Error: " + r.errorMessage);
                        printError("Duration: " +
                                Duration.between(r.startTime, r.endTime).toMillis() + " ms");
                        printInfo("");
                    });
        }

        printSeparator(SEPARATOR);
    }

    private void printInfo(String message) {
        terminal.writer().println(new AttributedStringBuilder()
                .append(message)
                .toAnsi());
        terminal.flush();
    }

    private void printError(String message) {
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                .append(message)
                .toAnsi());
        terminal.flush();
    }

    private void printSuccess(String message) {
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
                .append(message)
                .toAnsi());
        terminal.flush();
    }

    private void printSeparator(String separator) {
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE))
                .append(separator)
                .toAnsi());
        terminal.flush();
    }

    public enum ResultType {
        PASS,
        FAIL,
        SKIP
    }

    private static class TestResult {
        final String fileName;
        final String testName;
        final RunCommand.ResultType result;
        final LocalDateTime startTime;
        final LocalDateTime endTime;
        final String errorMessage;

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