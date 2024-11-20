package com.lithium.util.logger;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LithiumLogger {
    private LogLevel currentLevel = LogLevel.INFO;
    private String logFormat = "{level}: {message}";
    private String timestampFormat = "HH:mm:ss";
    private Terminal terminal;

    // Singleton pattern
    private static LithiumLogger instance;

    private LithiumLogger() {
        try {
            terminal = TerminalBuilder.builder()
                    .system(true)
                    .dumb(true)
                    .jansi(true)
                    .build();
        } catch (IOException e) {
            System.err.println("Failed to initialize terminal: " + e.getMessage());
        }
    }

    public static synchronized LithiumLogger getInstance() {
        if (instance == null) {
            instance = new LithiumLogger();
        }
        return instance;
    }

    // Set current log level
    public void setLogLevel(LogLevel level) {
        this.currentLevel = level;
    }

    // Set custom log format
    public void setLogFormat(String format) {
        this.logFormat = format;
    }

    // Core logging method
    public void log(LogLevel level, String message) {
        if (currentLevel == LogLevel.NONE || level.getPriority() > currentLevel.getPriority()) {
            return;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(timestampFormat);
            String timestamp = dateFormat.format(new Date());

            // Split the format into parts
            String[] parts = logFormat.split("\\{level\\}");
            String beforeLevel = parts[0].replace("{timestamp}", timestamp);
            String afterLevel = parts.length > 1 ? parts[1].replace("{message}", message) : "";

            // Build the complete message with only the level colored
            AttributedStringBuilder builder = new AttributedStringBuilder()
                    .append(beforeLevel)                           // Uncolored prefix
                    .style(getStyleForLevel(level))               // Apply color to level
                    .append(level.name())                         // Colored level
                    .style(AttributedStyle.DEFAULT)               // Reset style
                    .append(afterLevel);                          // Uncolored suffix

            if (terminal != null) {
                terminal.writer().println(builder.toAnsi());
                terminal.writer().flush();
            } else {
                System.out.println(beforeLevel + level.name() + afterLevel);
            }
        } catch (Exception e) {
            System.err.println("Logging failed: " + e.getMessage());
        }
    }

    // Get ANSI style for each log level
    private AttributedStyle getStyleForLevel(LogLevel level) {
        return switch (level) {
            case INFO -> AttributedStyle.DEFAULT.foreground(12); // CYAN
            case WARN -> AttributedStyle.DEFAULT.foreground(3);  // YELLOW
            case ERROR -> AttributedStyle.DEFAULT.foreground(1); // RED
            case DEBUG -> AttributedStyle.DEFAULT.foreground(2); // GREEN
            case TRACE -> AttributedStyle.DEFAULT;               // DEFAULT
            case FATAL -> AttributedStyle.DEFAULT.foreground(5); // MAGENTA
            default -> AttributedStyle.DEFAULT;
        };
    }

    public void setTimestampFormat(String format) {
        try {
            // Validate the format by creating a SimpleDateFormat
            new SimpleDateFormat(format);
            this.timestampFormat = format;
        } catch (IllegalArgumentException e) {
            // Optionally log an error or throw a custom exception
            error("Invalid timestamp format: " + format + ". Using default format.");
        }
    }

    // Convenience logging methods
    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public void trace(String message) {
        log(LogLevel.TRACE, message);
    }

    public void fatal(String message) {
        log(LogLevel.FATAL, message);
    }

    // Extras

    public void printSeparator(boolean isMain) {
        String SEPARATOR = "════════════════════════════════════════════════════════════";
        String SUB_SEPARATOR = "────────────────────────────────────────────────────────";
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(12))
                .append(isMain ? SEPARATOR : SUB_SEPARATOR)
                .toAnsi());
        terminal.flush();
    }

    public void title(String message) {
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(3))
                .append(message)
                .toAnsi());
        terminal.flush();
    }

    public void success(String message) {
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(2))
                .append(message)
                .toAnsi());
        terminal.flush();
    }

    public void fail(String message) {
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(1))
                .append(message)
                .toAnsi());
        terminal.flush();
    }

    public void basic(String message) {
        terminal.writer().println(message);
        terminal.flush();
    }
}