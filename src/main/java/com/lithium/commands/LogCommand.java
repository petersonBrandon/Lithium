package com.lithium.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Map;

public class LogCommand implements Command {
    private static final Logger log = LogManager.getLogger(LogCommand.class);

    private final String message;
    private final Level logLevel;
    private final Map<String, String> contextData;

    public LogCommand(String message, Level logLevel, Map<String, String> contextData) {
        this.message = message;
        this.logLevel = logLevel;
        this.contextData = contextData;
    }

    public LogCommand(String message, Level logLevel) {
        this(message, logLevel, Map.of());
    }

    public LogCommand(String message) {
        this(message, Level.INFO, Map.of());
    }

    @Override
    public void execute(WebDriver driver, WebDriverWait wait) {
        try {
            // Add any context data to the MDC
            contextData.forEach(ThreadContext::put);

            log.log(logLevel, message);
        } finally {
            // Clean up the MDC
            if (!contextData.isEmpty()) {
                ThreadContext.clearAll();
            }
        }
    }
}