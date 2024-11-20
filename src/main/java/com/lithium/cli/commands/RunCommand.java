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
    private final TestExecutionSummary summary;
    private ProjectConfig config;

    public RunCommand() {
        this.summary = new TestExecutionSummary();
    }

    @Override
    public String getDescription() {
        return "Executes Lithium test files";
    }

    @Override
    public String getUsage() {
        return "lit run <file-name> [test-name] [--headed=true|false] [--maximized=true|false] " +
                "[--browser=<browser_name>] [--timeout=<seconds>] [--threads=<thread_count>]";
    }

    @Override
    public void execute(String[] args) {
        validateArgsLength(args, 2);
        loadConfig();

        TestFileResolver fileResolver = new TestFileResolver(config);
        CommandLineArgsParser argsParser = new CommandLineArgsParser(config);

        Map<String, String> cliArgs = argsParser.parseArgs(args);
        String fileName = args[1];

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

    private record TestRunnerConfig(boolean headless, boolean maximized, String browser, int timeout, String baseUrl) {

        TestRunner createRunner() {
                return new TestRunner(headless, maximized, browser, timeout, baseUrl);
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

        summary.printSummary();
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
        LocalDateTime startTime = LocalDateTime.now();
        String errorMessage = null;
        ResultType result;
        TestRunner runner = null;
        ProjectConfig.ParallelExecutionConfig parallelConfig = config.getParallelExecution();

        if(!parallelConfig.isEnabled()) {
            log.printSeparator(false);
        }
        log.title("Running test: " + test.getName());

        try {
            runner = runnerConfig.createRunner();
            runner.runTest(test);
            result = ResultType.PASS;
            if(parallelConfig.isEnabled()) {
                log.success(String.format("%s Status: ✓ PASSED", test.getName()));
            } else {
                log.success("Status: ✓ PASSED");
            }
        } catch (Exception e) {
            result = ResultType.FAIL;
            errorMessage = e.getMessage();
            if(parallelConfig.isEnabled()) {
                log.error(String.format("%s Status: ✗ FAILED", test.getName()));
            } else {
                log.error("Status: ✗ FAILED");
            }
            log.error("Error: " + errorMessage);
        } finally {
            if (runner != null) {
                runner.close();
            }
        }

        LocalDateTime endTime = LocalDateTime.now();
        log.basic("Duration: " +
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
}