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
import com.lithium.core.LithiumTestHandler;
import com.lithium.core.TestCase;
import com.lithium.exceptions.LexerError;
import com.lithium.exceptions.TestSyntaxException;
import com.lithium.lexer.Lexer;
import com.lithium.lexer.Token;
import com.lithium.parser.Parser;
import com.lithium.parser.Stmt;
import com.lithium.util.logger.LithiumLogger;
import com.lithium.util.logger.LogLevel;
import com.lithium.util.reporter.LithiumReporter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        if (config.canCliOverride() && argsParser.argExists(cliArgs, "threads")) {
            config.getParallelExecution().setEnabled(true);
        }

        LogLevel logLevel = LogLevel.valueOf(config.getLogLevel().toUpperCase());
        log.setLogLevel(logLevel);

        boolean headless = !argsParser.getBooleanOption(cliArgs, "headed", !config.isHeadless());
        boolean maximized = argsParser.getBooleanOption(cliArgs, "maximized", config.isMaximizeWindow());
        String browser = argsParser.getStringOption(cliArgs, "browser", getEnvironmentBrowser(envConfig));

        try {
            List<String> testFilePaths = fileResolver.resolveTestFilePaths(fileName);
            String baseUrl = getEnvironmentBaseUrl(getEnvironmentConfig());

            runTests(testFilePaths, args);
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

    private void runTests(List<String> testFilePaths, String[] args)
            throws IOException, TestSyntaxException {
        Map<String, TestCase> testCases = new HashMap<>();

        for (String filePath : testFilePaths) {
            testCases.putAll(parseTestFile(filePath));
        }

        if (args.length > 2 && !args[2].startsWith("--")) {
            runSingleTest(args[2], testCases);
        } else {
            runAllTests(testCases);
        }

        testLogger.printSummary();
        generateReports(testLogger.getResults(), fileName);
    }

    private Map<String, TestCase> parseTestFile(String filePath) throws IOException, TestSyntaxException {
        String source = Files.readString(Path.of(filePath));
        Map<String, TestCase> testCases = new HashMap<>();

        try {
            // Lexical analysis
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.scanTokens();

            // Parsing
            Parser parser = new Parser(tokens);
            List<Stmt> statements = parser.parse();

            // Convert parsed statements to TestCase objects
            TestCaseBuilder builder = new TestCaseBuilder(filePath, statements);

            // First pass: collect all global statements (imports, functions, vars)
            List<Stmt> globalStatements = statements.stream()
                    .filter(stmt -> stmt instanceof Stmt.Import ||
                            stmt instanceof Stmt.Function ||
                            stmt instanceof Stmt.Var)
                    .collect(Collectors.toList());

            // Second pass: build test cases with global context
            for (Stmt stmt : statements) {
                if (stmt instanceof Stmt.Test testStmt) {
                    TestCase testCase = builder.buildTestCase(testStmt, globalStatements);
                    testCases.put(testCase.getName(), testCase);
                }
            }

        } catch (LexerError e) {
            throw new TestSyntaxException(String.format("Lexer error in file %s: %s", filePath, e.getMessage()));
        } catch (RuntimeException e) {
            throw new TestSyntaxException(String.format("Parser error in file %s: %s", filePath, e.getMessage()));
        }

        return testCases;
    }

    private record TestCaseBuilder(String filePath, List<Stmt> allStatements) {
        public TestCase buildTestCase(Stmt.Test test, List<Stmt> globalStatements) {
            String testName = test.description.getLiteral().toString();

            // Create a new list combining global statements and test body
            List<Stmt> combinedStatements = new ArrayList<>();

            // Add all global statements first
            combinedStatements.addAll(globalStatements);

            // Add test body statements
            combinedStatements.addAll(test.body);

            return new TestCase(testName, filePath, combinedStatements);
        }
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

    private void runSingleTest(String testName, Map<String, TestCase> testCases) {
        TestCase test = testCases.get(testName);
        if (test == null) {
            log.error("Test '" + testName + "' not found!");
            throw new IllegalArgumentException("Test '" + testName + "' not found!");
        }
        runAndLogTest(test);
    }

    private void runAllTests(Map<String, TestCase> testCases) {
        ProjectConfig.ParallelExecutionConfig parallelConfig = config.getParallelExecution();

        if (parallelConfig.isEnabled()) {
            // Use ExecutorService for parallel test execution
            ExecutorService executorService = Executors.newFixedThreadPool(parallelConfig.getThreadCount());

            List<CompletableFuture<Void>> futures = testCases.values().stream()
                    .map(test -> CompletableFuture.runAsync(() -> {
                        runAndLogTest(test);
                    }, executorService))
                    .collect(Collectors.toList());

            // Wait for all tests to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            executorService.shutdown();
        } else {
            // Existing sequential execution
            testCases.values().forEach(test -> runAndLogTest(test));
        }
    }

    private void runAndLogTest(TestCase test) {
        int maxRetries = config.getTestRetryCount();
        int currentAttempt = 0;
        LocalDateTime startTime = LocalDateTime.now();
        String errorMessage = null;
        ResultType result = ResultType.FAIL;
        ProjectConfig.ParallelExecutionConfig parallelConfig = config.getParallelExecution();

        if (!parallelConfig.isEnabled()) {
            log.printSeparator(false);
        }
        log.title("Running test: " + test.getName());

        while (currentAttempt <= maxRetries) {
            LithiumTestHandler fileHandler = null;
            try {
                // Log retry attempt
                if (currentAttempt > 0) {
                    log.warn(String.format("Retry Attempt %d for test: %s",
                            currentAttempt, test.getName()));
                }

                // Create and execute file handler
                fileHandler = new LithiumTestHandler(test.getStatements(), config);
                fileHandler.execute();

                result = ResultType.PASS;
                errorMessage = null;

                if (parallelConfig.isEnabled()) {
                    log.success(String.format("%s Status: ✓ PASSED", test.getName()));
                } else {
                    log.success("Status: ✓ PASSED");
                }
                break;  // Test passed, exit retry loop
            } catch (Exception e) {
                errorMessage = e.getMessage();

                if (parallelConfig.isEnabled()) {
                    log.fail(String.format("%s Attempt %d Failed: %s",
                            test.getName(), currentAttempt + 1, errorMessage));
                } else {
                    log.fail(String.format("Attempt %d Failed: %s",
                            currentAttempt + 1, errorMessage));
                }

                currentAttempt++;

                // If max retries reached, log final failure
                if (currentAttempt > maxRetries) {
                    if (parallelConfig.isEnabled()) {
                        log.fail(String.format("%s Status: ✗ FAILED (All retry attempts exhausted)", test.getName()));
                    } else {
                        log.fail("Status: ✗ FAILED (All retry attempts exhausted)");
                    }
                }
            } finally {
                if (fileHandler != null) {
                    fileHandler.close();
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
}