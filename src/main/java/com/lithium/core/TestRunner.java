/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: TestRunner.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.core;

import com.lithium.cli.util.ProjectConfig;
import com.lithium.commands.Command;
import com.lithium.commands.CommandFactory;
import com.lithium.exceptions.CommandException;
import com.lithium.parser.Expr;
import com.lithium.parser.Stmt;
import com.lithium.util.capture.ScreenshotCapture;
import com.lithium.util.logger.LithiumLogger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The TestRunner class is responsible for managing the WebDriver session and executing a series of commands
 * associated with a test case. It initializes the WebDriver in either headless or maximized mode,
 * depending on the specified options.
 */
public class TestRunner implements AutoCloseable {
    private static final LithiumLogger log = LithiumLogger.getInstance();

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final ProjectConfig config;
    private final Map<String, String> runtimeVariables;

    public TestRunner(boolean headless, boolean maximized, String browser, int timeout,
                      String baseUrl, ProjectConfig config) {
        this.config = config;
        this.runtimeVariables = new HashMap<>();

        BrowserDriverManager browserManager = new BrowserDriverManager();
        this.driver = browserManager.createDriver(browser, headless, maximized);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));

        // Set default timeouts
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeout));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeout));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(timeout));

        // Navigate to base URL if provided
        if (baseUrl != null && !baseUrl.isEmpty()) {
            log.info(String.format("Navigating to base URL: %s", baseUrl));
            try {
                driver.get(baseUrl);
            } catch (Exception e) {
                close();
                throw new CommandException("Error fetching url " + baseUrl);
            }
        }
    }

    public void runTest(TestCase test) {
        try {
            // Clear any runtime variables from previous test runs
            runtimeVariables.clear();

            // Execute each command directly from the parsed AST
            for (Stmt.Command cmd : test.getCommands()) {
                executeCommand(cmd);
            }
        } catch (Exception e) {
            ScreenshotCapture.captureScreenshot(driver, test.getName(), config);
            log.error(String.format("Test failed: %s", e.getMessage()));
            throw e;
        }
    }

    private void executeCommand(Stmt.Command cmd) {
        // Create execution context
        ExecutionContext context = new ExecutionContext(driver, wait, runtimeVariables);

        // Convert command arguments
        List<String> resolvedArgs = new ArrayList<>();
        for (Expr arg : cmd.arguments) {
            resolvedArgs.add(resolveExpression(arg, context));
        }

        // Convert named arguments
        Map<String, String> resolvedNamedArgs = new HashMap<>();
        for (Map.Entry<String, Expr> entry : cmd.namedArgs.entrySet()) {
            resolvedNamedArgs.put(entry.getKey(), resolveExpression(entry.getValue(), context));
        }

        // Create and execute the command
        Command command = CommandFactory.createCommand(
                cmd.commandType,
                resolvedArgs,
                cmd.locatorType,
                cmd.line
        );
        command.execute(context);
    }

    private String resolveExpression(Expr expr, ExecutionContext context) {
        if (expr instanceof Expr.Literal) {
            return ((Expr.Literal) expr).value.toString();
        } else if (expr instanceof Expr.Variable) {
            String varName = ((Expr.Variable) expr).name.getLexeme();
            String value = context.getVariable(varName);
            if (value == null) {
                throw new CommandException("Undefined variable: " + varName);
            }
            return value;
        }
        // Add support for other expression types as needed
        throw new CommandException("Unsupported expression type: " + expr.getClass().getSimpleName());
    }

    @Override
    public void close() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                log.error(String.format("Error while closing WebDriver: %s", e.getMessage()));
            }
        }
    }

    public static class ExecutionContext {
        private final WebDriver driver;
        private final WebDriverWait wait;
        private final Map<String, String> variables;

        private ExecutionContext(WebDriver driver, WebDriverWait wait, Map<String, String> variables) {
            this.driver = driver;
            this.wait = wait;
            this.variables = variables;
        }

        public WebDriver getDriver() {
            return driver;
        }

        public WebDriverWait getWait() {
            return wait;
        }

        public void setVariable(String name, String value) {
            variables.put(name, value);
        }

        public String getVariable(String name) {
            return variables.get(name);
        }
    }
}