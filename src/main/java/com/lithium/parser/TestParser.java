/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: TestParser.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.parser;

import com.lithium.commands.*;
import com.lithium.core.TestCase;
import com.lithium.exceptions.TestSyntaxException;
import com.lithium.locators.LocatorParser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The TestParser class is responsible for parsing Lithium test files (.lit) and creating TestCase objects.
 * It processes each line to interpret commands and build a map of test cases, which can then be executed.
 */
public class TestParser {
    private static final Logger log = LogManager.getLogger(TestParser.class);
    private static final Pattern TEST_PATTERN = Pattern.compile("test\\s+\"([^\"]+)\"\\s*\\{\\s*$");

    private final Map<String, TestCase> testCases;

    /**
     * Constructs a TestParser with an empty map of test cases.
     */
    public TestParser() {
        this.testCases = new HashMap<>();
    }

    /**
     * Parses the specified Lithium test file, building a map of test cases.
     *
     * @param filePath The path to the Lithium test file.
     * @return A map containing test cases parsed from the file, indexed by test name.
     * @throws IOException         If an I/O error occurs when reading the file.
     * @throws TestSyntaxException If there is a syntax error in the test file.
     */
    public Map<String, TestCase> parseFile(String filePath) throws IOException, TestSyntaxException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            TestCase currentTest = null;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                Matcher testMatcher = TEST_PATTERN.matcher(line);
                if (testMatcher.matches()) {
                    String testName = testMatcher.group(1);
                    currentTest = new TestCase(testName);
                    testCases.put(testName, currentTest);
                    log.info("Parsing test: " + testName);
                } else if (line.equals("}")) {
                    currentTest = null;
                } else if (currentTest != null) {
                    Command command = parseCommand(line, lineNumber);
                    if (command != null) {
                        currentTest.addCommand(command);
                    }
                }
            }

            if (currentTest != null) {
                throw new TestSyntaxException("Missing closing brace for test: " + currentTest.getName(), lineNumber);
            }
        }
        return testCases;
    }

    /**
     * Parses an individual command line within a test case, creating a Command object.
     *
     * @param line       The command line to parse.
     * @param lineNumber The line number in the test file, used for error reporting.
     * @return A Command object representing the parsed command.
     * @throws TestSyntaxException If there is an error in the command syntax.
     */
    private Command parseCommand(String line, int lineNumber) throws TestSyntaxException {
        try {
            String[] parts = line.trim().split("\\s+", 2);
            if (parts.length < 2) {
                throw new TestSyntaxException("Invalid command format", lineNumber);
            }

            String command = parts[0].toLowerCase();
            String remainingArgs = parts[1];

            return switch (command) {
                case "open" -> {
                    String url = extractQuotedString(remainingArgs, lineNumber);
                    yield new OpenCommand(url);
                }
                case "click" -> {
                    String[] clickArgs = parseLocatorArgs(remainingArgs, lineNumber);
                    yield new ClickCommand(LocatorParser.parse(clickArgs[0], clickArgs[1], lineNumber));
                }
                case "type" -> {
                    String[] typeArgs = parseTypeArgs(remainingArgs, lineNumber);
                    yield new TypeCommand(
                            LocatorParser.parse(typeArgs[0], typeArgs[1], lineNumber),
                            typeArgs[2]
                    );
                }
                case "wait" -> {
                    String[] waitArgs = parseWaitArgs(remainingArgs, lineNumber);
                    yield new WaitCommand(
                            LocatorParser.parse(waitArgs[0], waitArgs[1], lineNumber),
                            parseWaitType(waitArgs[2], lineNumber),
                            parseTimeout(waitArgs.length > 3 ? waitArgs[3] : "30", lineNumber)
                    );
                }
                case "log" -> {
                    String[] logArgs = parseLogArgs(remainingArgs, lineNumber);
                    yield createLogCommand(logArgs, lineNumber);
                }
                default -> {
                    log.warn("Unknown command at line " + lineNumber + ": " + command);
                    yield null;
                }
            };
        } catch (Exception e) {
            throw new TestSyntaxException("Error parsing command: " + e.getMessage(), lineNumber);
        }
    }

    /**
     * Extracts a quoted string from a command argument, handling syntax errors.
     *
     * @param input      The command argument containing the quoted string.
     * @param lineNumber The line number in the test file, used for error reporting.
     * @return The extracted string within the quotes.
     * @throws TestSyntaxException If the quoted string is missing or incomplete.
     */
    private String extractQuotedString(String input, int lineNumber) throws TestSyntaxException {
        int start = input.indexOf("\"");
        int end = input.indexOf("\"", start + 1);
        if (start == -1 || end == -1) {
            throw new TestSyntaxException("Missing quotes in command", lineNumber);
        }
        return input.substring(start + 1, end);
    }

    /**
     * Parses arguments for locator-based commands into type and value.
     *
     * @param args The argument string to parse
     * @param lineNumber The current line number for error reporting
     * @return Array containing [locatorType, locatorValue]
     * @throws TestSyntaxException if the arguments are invalid
     */
    private String[] parseLocatorArgs(String args, int lineNumber) throws TestSyntaxException {
        String[] parts = args.trim().split("\\s+", 2);
        if (parts.length != 2) {
            throw new TestSyntaxException("Invalid locator format. Expected: <type> \"value\"", lineNumber);
        }

        String locatorType = parts[0];
        String locatorValue = extractQuotedString(parts[1], lineNumber);

        return new String[]{locatorType, locatorValue};
    }

    /**
     * Parses arguments for the type command.
     *
     * @param args The argument string to parse
     * @param lineNumber The current line number for error reporting
     * @return Array containing [locatorType, locatorValue, textToType]
     * @throws TestSyntaxException if the arguments are invalid
     */
    private String[] parseTypeArgs(String args, int lineNumber) throws TestSyntaxException {
        // Split into locator type and remaining parts
        String[] parts = args.trim().split("\\s+", 2);
        if (parts.length != 2) {
            throw new TestSyntaxException("Invalid type command format. Expected: <locator_type> \"locator\" \"text\"", lineNumber);
        }

        String locatorType = parts[0];
        String remaining = parts[1].trim();

        // Extract the locator value (first quoted string)
        String locatorValue = extractQuotedString(remaining, lineNumber);

        // Find the text to type (second quoted string)
        int secondQuoteEnd = remaining.indexOf("\"", remaining.indexOf("\"") + 1) + 1;
        String textToType = extractQuotedString(remaining.substring(secondQuoteEnd).trim(), lineNumber);

        return new String[]{locatorType, locatorValue, textToType};
    }

    /**
     * Parses arguments for the wait command.
     *
     * @param args The argument string to parse
     * @param lineNumber The current line number for error reporting
     * @return Array containing [locatorType, locatorValue, visibility, timeout]
     * @throws TestSyntaxException if the arguments are invalid
     */
    private String[] parseWaitArgs(String args, int lineNumber) throws TestSyntaxException {
        String[] parts = args.trim().split("\\s+");
        if (parts.length < 3) {
            throw new TestSyntaxException(
                    "Invalid wait command format. Expected: <locator_type> \"locator\" <wait_type> [timeout]. " +
                            "Example: wait id \"submit-button\" visible 10",
                    lineNumber
            );
        }

        String locatorType = parts[0];
        String locatorValue = extractQuotedString(parts[1], lineNumber);
        String waitType = parts[2];

        // Create array with all parts
        String[] result = new String[parts.length];
        result[0] = locatorType;
        result[1] = locatorValue;
        result[2] = waitType;

        // Add timeout if specified
        if (parts.length > 3) {
            result[3] = parts[3];
        }

        return result;
    }

    /**
     * Parses the wait type string into a WaitCommand.WaitType enum value.
     *
     * @param waitTypeStr The wait type string to parse
     * @param lineNumber The current line number for error reporting
     * @return The corresponding WaitType enum value
     * @throws TestSyntaxException if the wait type is invalid
     */
    private WaitCommand.WaitType parseWaitType(String waitTypeStr, int lineNumber) throws TestSyntaxException {
        try {
            return switch (waitTypeStr.toLowerCase()) {
                case "presence" -> WaitCommand.WaitType.PRESENCE;
                case "visible" -> WaitCommand.WaitType.VISIBLE;
                case "clickable" -> WaitCommand.WaitType.CLICKABLE;
                default -> throw new IllegalArgumentException("Invalid wait type: " + waitTypeStr);
            };
        } catch (Exception e) {
            throw new TestSyntaxException(
                    "Invalid wait type '" + waitTypeStr + "'. Valid types are: presence, visible, clickable",
                    lineNumber
            );
        }
    }

    /**
     * Parses the timeout string into a long value.
     *
     * @param timeoutStr The timeout string to parse
     * @param lineNumber The current line number for error reporting
     * @return The timeout value in seconds
     * @throws TestSyntaxException if the timeout value is invalid
     */
    private long parseTimeout(String timeoutStr, int lineNumber) throws TestSyntaxException {
        try {
            long timeout = Long.parseLong(timeoutStr);
            if (timeout <= 0) {
                throw new TestSyntaxException(
                        "Timeout value must be positive. Got: " + timeout,
                        lineNumber
                );
            }
            return timeout;
        } catch (NumberFormatException e) {
            throw new TestSyntaxException(
                    "Invalid timeout value. Expected a positive number, got: " + timeoutStr,
                    lineNumber
            );
        }
    }

    /**
     * Parses arguments for the log command.
     *
     * @param args The argument string to parse
     * @param lineNumber The current line number for error reporting
     * @return Array containing [logType (optional), message]
     * @throws TestSyntaxException if the arguments are invalid
     */
    private String[] parseLogArgs(String args, int lineNumber) throws TestSyntaxException {
        args = args.trim();

        // Handle case with just message
        if (args.startsWith("\"")) {
            return new String[]{null, extractQuotedString(args, lineNumber), null};
        }

        // Split into type and remaining parts
        String[] parts = args.split("\\s+", 2);
        if (parts.length != 2) {
            throw new TestSyntaxException("Invalid log command format. Expected: log [level] \"message\" [context]", lineNumber);
        }

        String logLevel = parts[0];
        String remaining = parts[1];

        // Extract the message
        String message = extractQuotedString(remaining, lineNumber);

        // Look for context data after the message
        int endQuotePos = remaining.indexOf("\"", remaining.indexOf("\"") + 1);
        String contextData = null;
        if (endQuotePos + 1 < remaining.length()) {
            contextData = remaining.substring(endQuotePos + 1).trim();
        }

        return new String[]{logLevel, message, contextData};
    }

    /**
     * Creates a LogCommand from the parsed arguments.
     *
     * @param args Array containing [logType, message]
     * @param lineNumber The current line number for error reporting
     * @return A LogCommand instance
     * @throws TestSyntaxException if the log type is invalid
     */
    private LogCommand createLogCommand(String[] args, int lineNumber) throws TestSyntaxException {
        String logLevel = args[0];
        String message = args[1];
        String contextData = args[2];

        Level level = parseLogLevel(logLevel, lineNumber);

        if (contextData == null || contextData.isEmpty()) {
            return new LogCommand(message, level);
        }

        return new LogCommand(message, level, parseContextData(contextData, lineNumber));
    }

    /**
     * Parses the log level string into a Log4j Level.
     */
    private Level parseLogLevel(String logLevel, int lineNumber) throws TestSyntaxException {
        if (logLevel == null) {
            return Level.INFO;
        }

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

    /**
     * Parses context data string in the format "key1=value1 key2=value2" into a Map.
     */
    private Map<String, String> parseContextData(String contextData, int lineNumber) throws TestSyntaxException {
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
