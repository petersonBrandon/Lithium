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
import com.lithium.util.logger.LithiumLogger;
import com.lithium.util.logger.LogLevel;
import com.lithium.util.reporter.LithiumReporter;

import java.io.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class RunCommand extends BaseLithiumCommand {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private final TestExecutionLogger testLogger;
    private static ProjectConfig config;
    private String fileName;

    public RunCommand() {
        this.testLogger = new TestExecutionLogger();
    }

    @Override
    public String getDescription() {
        return "Executes Lithium test files";
    }

    @Override
    public String getUsage() {
        return "lithium run <file-name> [test-name] [--headed=true|false] [--maximized=true|false] " +
                "[--browser=<browser_name>] [--timeout=<seconds>] [--threads=<thread_count>]";
    }

    @Override
    public void execute(String[] args) {
        validateArgsLength(args, 2);
        loadConfig();

        TestFileResolver fileResolver = new TestFileResolver(config);
        CommandLineArgsParser argsParser = new CommandLineArgsParser(config);

        Map<String, String> cliArgs = argsParser.parseArgs(args);
        fileName = args[1];

        ProjectConfig.EnvironmentConfig envConfig = getEnvironmentConfig();

        int timeout = argsParser.getIntOption(cliArgs, "timeout", config.getDefaultTimeout());

        int threadCount = argsParser.getIntOption(cliArgs, "threads", config.getParallelExecution().getThreadCount());
        config.getParallelExecution().setThreadCount(threadCount);
        if(config.canCliOverride() && argsParser.argExists(cliArgs, "threads")) {
            config.getParallelExecution().setEnabled(true);
        }

        LogLevel logLevel = LogLevel.valueOf(config.getLogLevel().toUpperCase());
        log.setLogLevel(logLevel);

        // Use environment values with fallbacks
        boolean headless = !argsParser.getBooleanOption(cliArgs, "headed", !config.isHeadless());
        boolean maximized = argsParser.getBooleanOption(cliArgs, "maximized", config.isMaximizeWindow());
        String browser = argsParser.getStringOption(cliArgs, "browser",
                getEnvironmentBrowser(envConfig));

        try {
            List<String> testFilePaths = fileResolver.resolveTestFilePaths(fileName);
            String baseUrl = getEnvironmentBaseUrl(getEnvironmentConfig());
            TestRunnerConfig runnerConfig = new TestRunnerConfig(headless, maximized, browser, timeout, baseUrl);

            runTests(testFilePaths, args, runnerConfig);
        } catch (Exception e) {
            log.error("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private ProjectConfig.EnvironmentConfig getEnvironmentConfig() {
        String activeEnv = config.getActiveEnvironment();
        if (activeEnv != null && !activeEnv.isEmpty()) {
            Map<String, ProjectConfig.EnvironmentConfig> environments = config.getEnvironments();
            if (environments != null && environments.containsKey(activeEnv)) {
                return environments.get(activeEnv);
            }
        }
        return null;
    }

    private String getEnvironmentBrowser(ProjectConfig.EnvironmentConfig envConfig) {
        if (envConfig != null && envConfig.getBrowser() != null && !envConfig.getBrowser().isEmpty()) {
            return envConfig.getBrowser();
        }
        return config.getBrowser();
    }

    private String getEnvironmentBaseUrl(ProjectConfig.EnvironmentConfig envConfig) {
        if (envConfig != null && envConfig.getBaseUrl() != null && !envConfig.getBaseUrl().isEmpty()) {
            return envConfig.getBaseUrl();
        }
        return config.getBaseUrl();
    }

    private void runTests(List<String> testFilePaths, String[] args, TestRunnerConfig runnerConfig)
            throws IOException, TestSyntaxException {
        TestParser parser = new TestParser();
        Map<String, TestCase> testCases = new HashMap<>();

        for (String filePath : testFilePaths) {
            testCases.putAll(parser.parseFile(filePath));
        }

        if (args.length > 2 && !args[2].startsWith("--")) {
            runSingleTest(args[2], testCases, runnerConfig);
        } else {
            runAllTests(testCases, runnerConfig);
        }

        testLogger.printSummary();
        generateReports(testLogger.getResults(), fileName);
    }

    private void generateReports(List<TestResult> testResults, String testSource) {
        if (config.getReportFormat() != null && config.getReportFormat().length != 0) {
            LithiumReporter reporter = new LithiumReporter(
                    config.getReportDirectory(),
                    List.of(config.getReportFormat()),
                    config.getProjectName(),
                    testSource
            );
            reporter.generateReports(testResults);
        }
    }

    private void runSingleTest(String testName, Map<String, TestCase> testCases, TestRunnerConfig runnerConfig) {
        TestCase test = testCases.get(testName);
        if (test == null) {
            log.error("Test '" + testName + "' not found!");
            throw new IllegalArgumentException("Test '" + testName + "' not found!");
        }
        runAndLogTest(test, runnerConfig);
    }

    private void runAllTests(Map<String, TestCase> testCases, TestRunnerConfig runnerConfig) {
        ProjectConfig.ParallelExecutionConfig parallelConfig = config.getParallelExecution();

        if (parallelConfig.isEnabled()) {
            // Use ExecutorService for parallel test execution
            ExecutorService executorService = Executors.newFixedThreadPool(parallelConfig.getThreadCount());

            List<CompletableFuture<Void>> futures = testCases.values().stream()
                    .map(test -> CompletableFuture.runAsync(() -> {
                        runAndLogTest(test, runnerConfig);
                    }, executorService))
                    .collect(Collectors.toList());

            // Wait for all tests to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            executorService.shutdown();
        } else {
            // Existing sequential execution
            testCases.values().forEach(test -> runAndLogTest(test, runnerConfig));
        }
    }

    private void runAndLogTest(TestCase test, TestRunnerConfig runnerConfig) {
        int maxRetries = config.getTestRetryCount();
        int currentAttempt = 0;
        LocalDateTime startTime = LocalDateTime.now();
        String errorMessage = null;
        ResultType result = ResultType.FAIL;
        ProjectConfig.ParallelExecutionConfig parallelConfig = config.getParallelExecution();

        if(!parallelConfig.isEnabled()) {
            log.printSeparator(false);
        }
        log.title("Running test: " + test.getName());

        while (currentAttempt <= maxRetries) {
            TestRunner runner = null;
            try {
                // Log retry attempt
                if (currentAttempt > 0) {
                    log.warn(String.format("Retry Attempt %d for test: %s",
                            currentAttempt, test.getName()));
                }

                runner = runnerConfig.createRunner();
                runner.runTest(test);
                result = ResultType.PASS;
                errorMessage = null;

                if(parallelConfig.isEnabled()) {
                    log.success(String.format("%s Status: ✓ PASSED", test.getName()));
                } else {
                    log.success("Status: ✓ PASSED");
                }
                break;  // Test passed, exit retry loop
            } catch (Exception e) {
                errorMessage = e.getMessage();

                if(parallelConfig.isEnabled()) {
                    log.fail(String.format("%s Attempt %d Failed: %s",
                            test.getName(), currentAttempt + 1, errorMessage));
                } else {
                    log.fail(String.format("Attempt %d Failed: %s",
                            currentAttempt + 1, errorMessage));
                }

                currentAttempt++;

                // If max retries reached, log final failure
                if (currentAttempt > maxRetries) {
                    if(parallelConfig.isEnabled()) {
                        log.fail(String.format("%s Status: ✗ FAILED (All retry attempts exhausted)", test.getName()));
                    } else {
                        log.fail("Status: ✗ FAILED (All retry attempts exhausted)");
                    }
                }
            } finally {
                if (runner != null) {
                    runner.close();
                }
            }
        }

        LocalDateTime endTime = LocalDateTime.now();
        log.basic("Duration: " +
                java.time.Duration.between(startTime, endTime).toMillis() + " ms");

        testLogger.addResult(new TestResult(
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
            log.error("Error loading config: " + e.getMessage());
            config = new ProjectConfig("Lithium Project");
        }
    }

    private void validateTestDirectory() {
        if (config.getTestDirectory() != null) {
            File testDir = new File(config.getTestDirectory());
            if (!testDir.exists() || !testDir.isDirectory()) {
                log.error("Warning: Configured test directory '" +
                        config.getTestDirectory() + "' does not exist. Falling back to current directory.");
                config.setTestDirectory(null);
            }
        }
    }

    private record TestRunnerConfig(boolean headless, boolean maximized, String browser, int timeout, String baseUrl) {

        TestRunner createRunner() {
            return new TestRunner(headless, maximized, browser, timeout, baseUrl, config);
        }
    }
}