package com.lithium.parser.utils;

import com.lithium.commands.LogCommand;
import com.lithium.exceptions.TestSyntaxException;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.Map;

public class LogUtils {
    public static String[] parseLogArgs(String args, int lineNumber) throws TestSyntaxException {
        args = args.trim();

        if (args.startsWith("\"")) {
            return new String[]{null, StringUtils.extractQuotedString(args, lineNumber), null};
        }

        String[] parts = args.split("\\s+", 2);
        if (parts.length != 2) {
            throw new TestSyntaxException("Invalid log command format", lineNumber);
        }

        String logLevel = parts[0];
        String remaining = parts[1];
        String message = StringUtils.extractQuotedString(remaining, lineNumber);

        int endQuotePos = remaining.indexOf("\"", remaining.indexOf("\"") + 1);
        String contextData = null;
        if (endQuotePos + 1 < remaining.length()) {
            contextData = remaining.substring(endQuotePos + 1).trim();
        }

        return new String[]{logLevel, message, contextData};
    }

    public static LogCommand createLogCommand(String[] args, int lineNumber) throws TestSyntaxException {
        String logLevel = args[0];
        String message = args[1];
        String contextData = args[2];

        Level level = parseLogLevel(logLevel, lineNumber);

        if (contextData == null || contextData.isEmpty()) {
            return new LogCommand(message, level);
        }

        return new LogCommand(message, level, parseContextData(contextData, lineNumber));
    }

    private static Level parseLogLevel(String logLevel, int lineNumber) throws TestSyntaxException {
        if (logLevel == null) return Level.INFO;

        return switch (logLevel.toLowerCase()) {
            case "trace" -> Level.TRACE;
            case "debug" -> Level.DEBUG;
            case "info" -> Level.INFO;
            case "warn" -> Level.WARN;
            case "error" -> Level.ERROR;
            case "fatal" -> Level.FATAL;
            default -> throw new TestSyntaxException(
                    "Invalid log level '" + logLevel + "'. Valid levels are: trace, debug, info, warn, error, fatal",
                    lineNumber
            );
        };
    }

    private static Map<String, String> parseContextData(String contextData, int lineNumber) throws TestSyntaxException {
        Map<String, String> result = new HashMap<>();
        String[] pairs = contextData.split("\\s+");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length != 2) {
                throw new TestSyntaxException(
                        "Invalid context data format. Expected 'key=value', got: " + pair,
                        lineNumber
                );
            }
            result.put(keyValue[0], keyValue[1]);
        }

        return result;
    }
}