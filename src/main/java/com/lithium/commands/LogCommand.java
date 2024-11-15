package com.lithium.commands;

import com.lithium.core.TestContext;
import com.lithium.exceptions.CommandException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Map;

/**
 * The LogCommand class is used to log messages at a specified log level,
 * with optional context data added to the MDC (Mapped Diagnostic Context).
 * This command can be executed during a Selenium WebDriver test.
 */
public class LogCommand implements Command {
    private static final Logger log = LogManager.getLogger(LogCommand.class);
    private final String message;
    private final Level logLevel;

    // Context data to be added to the MDC for logging purposes
    private final Map<String, String> contextData;

    /**
     * Constructs a LogCommand with the specified message, log level, and context data.
     *
     * @param message The message to be logged.
     * @param logLevel The log level for the message.
     * @param contextData A map of context data to be added to the MDC.
     */
    public LogCommand(String message, Level logLevel, Map<String, String> contextData) {
        this.message = message;
        this.logLevel = logLevel;
        this.contextData = contextData;
    }

    /**
     * Constructs a LogCommand with the specified message and log level.
     * Context data is set to an empty map.
     *
     * @param message The message to be logged.
     * @param logLevel The log level for the message.
     */
    public LogCommand(String message, Level logLevel) {
        this(message, logLevel, Map.of());
    }

    /**
     * Constructs a LogCommand with the specified message.
     * The log level is set to Level.INFO, and context data is set to an empty map.
     *
     * @param message The message to be logged.
     */
    public LogCommand(String message) {
        this(message, Level.INFO, Map.of());
    }

    /**
     * Executes the log command by logging the message at the specified log level.
     * Any provided context data is added to the MDC before logging, and cleaned up afterward.
     *
     * @param driver The Selenium WebDriver instance (not used in this command).
     * @param wait The WebDriverWait instance (not used in this command).
     */
    @Override
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        try {
            // Add any context data to the MDC (Mapped Diagnostic Context)
            contextData.forEach(ThreadContext::put);

            log.log(logLevel, context.resolveVariables(message));
        } catch (Exception e) {
            throw new CommandException(String.format(
                    "Failed to execute log command. Message: '%s', LogLevel: '%s'",
                    message, logLevel
            ));
        } finally {
            // Clean up the MDC to avoid any unintended side effects
            if (!contextData.isEmpty()) {
                ThreadContext.clearAll();
            }
        }
    }
}
