package com.lithium.commands.utility;

import com.lithium.commands.Command;
import com.lithium.core.TestContext;
import com.lithium.exceptions.CommandException;
import com.lithium.util.logger.LithiumLogger;
import com.lithium.util.logger.LogLevel;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Map;

/**
 * The LogCommand class is used to log messages at a specified log level,
 * with optional context data added to the MDC (Mapped Diagnostic Context).
 * This command can be executed during a Selenium WebDriver test.
 */
public class LogCommand implements Command {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private final String message;
    private final LogLevel logLevel;

    /**
     * Constructs a LogCommand with the specified message, log level, and context data.
     *
     * @param message The message to be logged.
     * @param logLevel The log level for the message.
     */
    public LogCommand(String message, LogLevel logLevel) {
        this.message = message;
        this.logLevel = logLevel;
    }

    /**
     * Constructs a LogCommand with the specified message.
     * The log level is set to LogLevel.INFO, and context data is set to an empty map.
     *
     * @param message The message to be logged.
     */
    public LogCommand(String message) {
        this(message, LogLevel.INFO);
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
            log.log(logLevel, context.resolveVariables(message));
        } catch (Exception e) {
            throw new CommandException(String.format(
                    "Failed to execute log command. Message: '%s', LogLevel: '%s'",
                    message, logLevel
            ));
        }
    }
}
