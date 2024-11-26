package com.lithium.core;

import com.lithium.cli.util.ProjectConfig;
import com.lithium.commands.Command;
import com.lithium.commands.CommandFactory;
import com.lithium.exceptions.CommandException;
import com.lithium.locators.LocatorType;
import com.lithium.parser.Expr;
import com.lithium.parser.Stmt;
import com.lithium.util.capture.ScreenshotCapture;
import com.lithium.util.logger.LithiumLogger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class LithiumTestHandler implements AutoCloseable {
    private static final LithiumLogger log = LithiumLogger.getInstance();

    private final List<Stmt> statements;
    private final Interpreter interpreter;
    private final Map<String, Object> globalVariables;
    private final Map<String, Stmt.Function> functions;
    private final Map<String, Object> imports;
    private final Map<String, Object> exports;

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final ProjectConfig config;

    // Context for control flow statements
    private boolean isReturning = false;
    private boolean isContinuing = false;
    private boolean isBreaking = false;

    public LithiumTestHandler(List<Stmt> statements, ProjectConfig config) {
        this.statements = statements;
        BrowserDriverManager browserManager = new BrowserDriverManager();
        this.driver = browserManager.createDriver(config.getBrowser(), config.isHeadless(), config.isMaximizeWindow());
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(config.getDefaultTimeout()));

        // Set default timeouts
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(config.getDefaultTimeout()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.getDefaultTimeout()));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(config.getDefaultTimeout()));

        this.config = config;
        this.globalVariables = new HashMap<>();
        this.functions = new HashMap<>();
        this.imports = new HashMap<>();
        this.exports = new HashMap<>();
        this.interpreter = new Interpreter(this.globalVariables, this.functions, this);

        // Navigate to base URL if provided
        if (config.getBaseUrl() != null && !config.getBaseUrl().isEmpty()) {
            log.info(String.format("Navigating to base URL: %s", config.getBaseUrl()));
            try {
                driver.get(config.getBaseUrl());
            } catch (Exception e) {
                close();
                throw new CommandException("Error fetching url " + config.getBaseUrl());
            }
        }
    }

    public void execute() {
        try {
            for (Stmt statement : statements) {
                processStatement(statement);

                // Reset control flow flags after each statement
                if (isReturning || isContinuing || isBreaking) {
                    break;
                }
            }
        } catch (Exception e) {
            throw new CommandException("Execution failed: " + e.getMessage());
        }
    }

    public boolean isReturning() {
        return isReturning;
    }

    public boolean isBreaking() {
        return isBreaking;
    }

    public boolean isContinuing() {
        return isContinuing;
    }

    public void clearControlFlowFlags() {
        isReturning = false;
        isBreaking = false;
        isContinuing = false;
    }

    void processStatement(Stmt statement) {
        if (isReturning || isContinuing || isBreaking) return;

        try {
            if (statement instanceof Stmt.Var varStmt) {
                handleVariableDeclaration(varStmt);
            } else if (statement instanceof Stmt.Function funcStmt) {
                handleFunctionDeclaration(funcStmt);
            } else if (statement instanceof Stmt.Import importStmt) {
                handleImportStatement(importStmt);
            } else if (statement instanceof Stmt.Export exportStmt) {
                handleExportStatement(exportStmt);
            } else if (statement instanceof Stmt.Command cmdStmt) {
                handleCommandStatement(cmdStmt);
            } else if (statement instanceof Stmt.Block blockStmt) {
                handleBlockStatement(blockStmt);
            } else if (statement instanceof Stmt.If ifStmt) {
                handleIfStatement(ifStmt);
            } else if (statement instanceof Stmt.While whileStmt) {
                handleWhileStatement(whileStmt);
            } else if (statement instanceof Stmt.For forStmt) {
                handleForStatement(forStmt);
            } else if (statement instanceof Stmt.Return returnStmt) {
                handleReturnStatement(returnStmt);
            } else if (statement instanceof Stmt.Test testStmt) {
                handleTestStatement(testStmt);
            } else if (statement instanceof Stmt.Expression exprStmt) {
                handleExpressionStatement(exprStmt);
            }
        } catch (Exception e) {
            throw new CommandException("Statement processing failed: " + e.getMessage());
        }
    }

    private void handleVariableDeclaration(Stmt.Var varStmt) {
        Object value = varStmt.initializer != null
                ? interpreter.evaluate(varStmt.initializer)
                : null;
        interpreter.defineVariable(varStmt.name.getLexeme(), value);
    }

    private void handleFunctionDeclaration(Stmt.Function funcStmt) {
        functions.put(funcStmt.name.getLexeme(), funcStmt);
    }

    private void handleImportStatement(Stmt.Import importStmt) {
        String path = (String) importStmt.path.getLiteral();
        String alias = importStmt.alias != null
                ? importStmt.alias.getLexeme()
                : extractModuleName(path);

        // Todo: Simulated import - in real implementation, this would use a module loader
        try {
            // Placeholder for actual import mechanism
            Object importedModule = loadModule(path);
            imports.put(alias, importedModule);
        } catch (Exception e) {
            throw new CommandException("Import failed: " + path);
        }
    }

    private void handleExportStatement(Stmt.Export exportStmt) {
        if (exportStmt.exportAll) {
            // Export all current global variables and functions
            exports.putAll(globalVariables);
            exports.putAll(functions);
        } else if (exportStmt.name != null) {
            String symbolName = exportStmt.name.getLexeme();
            Object symbolValue = globalVariables.get(symbolName);

            if (symbolValue == null) {
                symbolValue = functions.get(symbolName);
            }

            if (symbolValue != null) {
                exports.put(symbolName, symbolValue);
            } else {
                throw new CommandException("Cannot export undefined symbol: " + symbolName);
            }
        }
    }

    private void handleCommandStatement(Stmt.Command cmdStmt) {
        try {
            ExecutionContext context = new ExecutionContext(driver, wait);

            // Prepare arguments by converting to strings
            List<String> resolvedArgs = cmdStmt.arguments.stream()
                    .map(arg -> String.valueOf(interpreter.evaluate(arg)))
                    .collect(Collectors.toList());

            // Use CommandFactory to create and execute the command
            Command command = CommandFactory.createCommand(
                    cmdStmt.commandType,
                    resolvedArgs,
                    cmdStmt.locatorType != null ? cmdStmt.locatorType : LocatorType.CSS,
                    cmdStmt.line
            );
            command.execute(context);
        } catch (Exception e) {
            throw new CommandException("Command failed: " + e.getMessage());
        }
    }

    private void handleBlockStatement(Stmt.Block blockStmt) {
        for (Stmt stmt : blockStmt.statements) {
            processStatement(stmt);

            if (isReturning || isContinuing || isBreaking) {
                break;
            }
        }
    }

    private void handleIfStatement(Stmt.If ifStmt) {
        boolean conditionResult = (Boolean) interpreter.evaluate(ifStmt.condition);

        if (conditionResult) {
            for (Stmt stmt : ifStmt.thenBranch) {
                processStatement(stmt);
                if (isReturning || isContinuing || isBreaking) break;
            }
        } else if (ifStmt.elseBranch != null) {
            for (Stmt stmt : ifStmt.elseBranch) {
                processStatement(stmt);
                if (isReturning || isContinuing || isBreaking) break;
            }
        }
    }

    private void handleWhileStatement(Stmt.While whileStmt) {
        while ((Boolean) interpreter.evaluate(whileStmt.condition)) {
            for (Stmt stmt : whileStmt.body) {
                processStatement(stmt);

                if (isReturning) return;
                if (isBreaking) {
                    isBreaking = false;
                    return;
                }
                if (isContinuing) {
                    isContinuing = false;
                    break;  // Break inner loop to continue while loop
                }
            }
        }
    }

    private void handleForStatement(Stmt.For forStmt) {
        if (forStmt.isRangeBased) {
            // Existing range-based for loop logic
            Expr.Range rangeExpr = (Expr.Range) forStmt.range;
            int start = (Integer) interpreter.evaluate(rangeExpr.start);
            int end = (Integer) interpreter.evaluate(rangeExpr.end);

            for (int i = start; i <= end; i++) {
                // Set loop variable
                globalVariables.put(forStmt.variable.getLexeme(), i);

                for (Stmt stmt : forStmt.body) {
                    processStatement(stmt);

                    if (isReturning) return;
                    if (isBreaking) {
                        isBreaking = false;
                        return;
                    }
                    if (isContinuing) {
                        isContinuing = false;
                        break;  // Break inner loop to continue for loop
                    }
                }
            }
        } else {
            // Traditional C-style for loop
            // Execute initialization
            if (forStmt.initialization != null) {
                processStatement(forStmt.initialization);
            }

            // Continue loop while condition is true (or indefinitely if no condition)
            while (forStmt.condition == null ||
                    (Boolean) interpreter.evaluate(forStmt.condition)) {

                for (Stmt stmt : forStmt.body) {
                    processStatement(stmt);

                    if (isReturning) return;
                    if (isBreaking) {
                        isBreaking = false;
                        return;
                    }
                    if (isContinuing) {
                        isContinuing = false;
                        break;  // Break inner loop to continue for loop
                    }
                }

                // Execute increment
                if (forStmt.increment != null) {
                    interpreter.evaluate(forStmt.increment);
                }
            }
        }
    }

    private void handleReturnStatement(Stmt.Return returnStmt) {
        Object returnValue = returnStmt.value != null
                ? interpreter.evaluate(returnStmt.value)
                : null;
        isReturning = true;
    }

    private void handleTestStatement(Stmt.Test testStmt) {
        String description = (String) testStmt.description.getLiteral();
        try {
            for (Stmt stmt : testStmt.body) {
                processStatement(stmt);
            }
            log.info("Test passed: " + description);
        } catch (Exception e) {
            ScreenshotCapture.captureScreenshot(driver, description, config);
            throw new CommandException("Test failed: " + description);
        }
    }

    private void handleExpressionStatement(Stmt.Expression exprStmt) {
        interpreter.evaluate(exprStmt.expression);
    }

    private Object loadModule(String path) {
        // Todo:
        // Placeholder for module loading logic
        // In a real implementation, this would use a module system
        return new Object();
    }

    private String extractModuleName(String path) {
        int lastSeparator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        int extensionStart = path.lastIndexOf('.');
        return path.substring(lastSeparator + 1, extensionStart);
    }

    // Getter methods
    public Map<String, Object> getGlobalVariables() {
        return new HashMap<>(globalVariables);
    }

    public Map<String, Stmt.Function> getFunctions() {
        return new HashMap<>(functions);
    }

    public Map<String, Object> getImports() {
        return new HashMap<>(imports);
    }

    public Map<String, Object> getExports() {
        return new HashMap<>(exports);
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
}