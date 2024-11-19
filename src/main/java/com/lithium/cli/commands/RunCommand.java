/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: RunCommand.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.cli.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lithium.cli.BaseLithiumCommand;
import com.lithium.cli.util.*;
import com.lithium.core.TestCase;
import com.lithium.core.TestRunner;
import com.lithium.exceptions.TestSyntaxException;
import com.lithium.parser.TestParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public class RunCommand extends BaseLithiumCommand {
    private final TerminalOutput output;
    private final TestExecutionSummary summary;
    private ProjectConfig config;

    public RunCommand() {
        try {
            Terminal terminal = TerminalBuilder.builder()
                    .system(true)
                    .dumb(true)
                    .jansi(true)
                    .build();
            this.output = new TerminalOutput(terminal);
            this.summary = new TestExecutionSummary(output);
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
        return "lit run <file-name> [test-name] [--headed=true|false] [--maximized=true|false] " +
                "[--browser=<browser_name>] [--timeout=<seconds>]";
    }

    @Override
    public void execute(String[] args) {
        validateArgsLength(args, 2);
        loadConfig();

        TestFileResolver fileResolver = new TestFileResolver(config);
        CommandLineArgsParser argsParser = new CommandLineArgsParser(config);

        Map<String, String> cliArgs = argsParser.parseArgs(args);
        String fileName = args[1];

        boolean headless = !argsParser.getBooleanOption(cliArgs, "headed", !config.isHeadless());
        boolean maximized = argsParser.getBooleanOption(cliArgs, "maximized", config.isMaximizeWindow());
        String browser = argsParser.getStringOption(cliArgs, "browser", config.getBrowser());
        int timeout = argsParser.getIntOption(cliArgs, "timeout", config.getDefaultTimeout());

        TestRunner runner = null;

        try {
            String testFilePath = fileResolver.resolveTestFilePath(fileName);
            runTests(testFilePath, args, headless, maximized, browser, timeout);
        } catch (Exception e) {
            output.printError("Error: " + e.getMessage());
            System.exit(1);
        } finally {
            if (runner != null) {
                runner.close();
            }
        }
    }

    private void runTests(String testFilePath, String[] args, boolean headless,
                          boolean maximized, String browser, int timeout)
            throws IOException, TestSyntaxException {
        TestParser parser = new TestParser();
        Map<String, TestCase> testCases = parser.parseFile(testFilePath);
        TestRunner runner = new TestRunner(headless, maximized, browser, timeout);

        if (args.length > 2 && !args[2].startsWith("--")) {
            runSingleTest(args[2], testCases, runner);
        } else {
            runAllTests(testCases, runner);
        }

        summary.printSummary();
    }

    private void runSingleTest(String testName, Map<String, TestCase> testCases, TestRunner runner) {
        TestCase test = testCases.get(testName);
        if (test == null) {
            output.printError("Test '" + testName + "' not found!");
            throw new IllegalArgumentException("Test '" + testName + "' not found!");
        }
        runAndLogTest(runner, test);
    }

    private void runAllTests(Map<String, TestCase> testCases, TestRunner runner) {
        testCases.values().forEach(test -> runAndLogTest(runner, test));
    }

    private void runAndLogTest(TestRunner runner, TestCase test) {
        LocalDateTime startTime = LocalDateTime.now();
        String errorMessage = null;
        ResultType result;

        output.printSeparator(false);
        output.printInfo("Running test: " + test.getName());

        try {
            runner.runTest(test);
            result = ResultType.PASS;
            output.printSuccess("Status: ✓ PASSED");
        } catch (Exception e) {
            result = ResultType.FAIL;
            errorMessage = e.getMessage();
            output.printError("Status: ✗ FAILED");
            output.printError("Error: " + errorMessage);
        }

        LocalDateTime endTime = LocalDateTime.now();
        output.printInfo("Duration: " +
                java.time.Duration.between(startTime, endTime).toMillis() + " ms");

        summary.addResult(new TestResult(
                test.getName(),
                test.getName(),
                result,
                startTime,
                endTime,
                errorMessage
        ));
    }

    private void loadConfig() {
        try {
            String configPath = System.getProperty("user.dir") + "/lithium.config.json";
            File configFile = new File(configPath);
            if (configFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                config = mapper.readValue(configFile, ProjectConfig.class);
                validateTestDirectory();
            } else {
                config = new ProjectConfig("Lithium Project");
            }
        } catch (IOException e) {
            output.printError("Error loading config: " + e.getMessage());
            config = new ProjectConfig("Lithium Project");
        }
    }

    private void validateTestDirectory() {
        if (config.getTestDirectory() != null) {
            File testDir = new File(config.getTestDirectory());
            if (!testDir.exists() || !testDir.isDirectory()) {
                output.printError("Warning: Configured test directory '" +
                        config.getTestDirectory() + "' does not exist. Falling back to current directory.");
                config.setTestDirectory(null);
            }
        }
    }
}