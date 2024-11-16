/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: CommandParser.java
 * Author: Brandon Peterson
 * Date: 11/14/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.parser;

import com.lithium.commands.*;
import com.lithium.exceptions.TestSyntaxException;
import com.lithium.parser.commandTypes.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The CommandParser class is responsible for parsing script commands in the
 * Lithium Automation Framework. It reads a single line of a test script, interprets
 * the command and its arguments, and returns the corresponding Command object
 * for execution.
 */
public class CommandParser {
    private static final Logger log = LogManager.getLogger(CommandParser.class);

    /**
     * Parses a single script command line and returns the corresponding Command object.
     *
     * @param line       the script line containing the command and arguments
     * @param lineNumber the line number of the script (for error reporting)
     * @return the parsed Command object
     * @throws TestSyntaxException if the command syntax is invalid
     */
    public Command parseCommand(String line, int lineNumber) throws TestSyntaxException {
        // First check if the line ends with a semicolon
        line = line.trim();
        if (!line.endsWith(";")) {
            throw new TestSyntaxException("Missing semicolon at end of command", lineNumber);
        }

        // Remove the semicolon before parsing
        line = line.substring(0, line.length() - 1).trim();

        String command = line;
        String remainingArgs = "";

        if (line.contains(" ")) {
            String[] parts = line.split("\\s+", 2);
            command = parts[0];
            remainingArgs = parts.length > 1 ? parts[1] : "";
        }

        return switch (command) {

            // Assertions

            case "assertText" -> AssertionsParser.parseAssertTextCommand(remainingArgs, lineNumber);
            case "assertVisible" -> AssertionsParser.parseAssertVisibleCommand(remainingArgs, lineNumber);
            case "assertURL" -> AssertionsParser.parseAssertURLCommand(remainingArgs, lineNumber);

            // Interaction

            case "click" -> InteractionParser.parseClickCommand(remainingArgs, lineNumber);
            case "type" -> InteractionParser.parseTypeCommand(remainingArgs, lineNumber);

            // Navigation

            case "open" -> NavigationParser.parseOpenCommand(remainingArgs, lineNumber);
            case "back" -> NavigationParser.parseBackCommand(remainingArgs, lineNumber);
            case "forward" -> NavigationParser.parseForwardCommand(remainingArgs, lineNumber);
            case "refresh" -> NavigationParser.parseRefreshCommand(remainingArgs, lineNumber);
            case "switchToWindow" -> NavigationParser.parseSwitchToWindowCommand(remainingArgs, lineNumber);
            case "openTab" -> NavigationParser.parseOpenTabCommand(remainingArgs, lineNumber);

            // Utility

            case "set" -> UtilityParser.parseSetCommand(remainingArgs, lineNumber);
            case "log" -> UtilityParser.parseLogCommand(remainingArgs, lineNumber);

            // Wait

            case "wait" -> WaitParser.parseWaitCommand(remainingArgs, lineNumber);

            default -> null;
        };
    }
}
