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
import com.lithium.commands.assertions.AssertTextCommand;
import com.lithium.commands.assertions.AssertURLCommand;
import com.lithium.commands.assertions.AssertVisibleCommand;
import com.lithium.exceptions.TestSyntaxException;
import com.lithium.locators.LocatorParser;
import com.lithium.parser.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

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

        String[] parts = line.split("\\s+", 2);
        if (parts.length < 2) {
            throw new TestSyntaxException("Invalid command format", lineNumber);
        }

        String command = parts[0];
        String remainingArgs = parts[1];

        return switch (command) {
            case "open" -> parseOpenCommand(remainingArgs, lineNumber);
            case "click" -> parseClickCommand(remainingArgs, lineNumber);
            case "type" -> parseTypeCommand(remainingArgs, lineNumber);
            case "wait" -> parseWaitCommand(remainingArgs, lineNumber);
            case "log" -> parseLogCommand(remainingArgs, lineNumber);
            case "set" -> parseSetCommand(remainingArgs, lineNumber);
            case "assertText" -> parseAssertTextCommand(remainingArgs, lineNumber);
            case "assertVisible" -> parseAssertVisibleCommand(remainingArgs, lineNumber);
            case "assertURL" -> parseAssertURLCommand(remainingArgs, lineNumber);
            default -> null;
        };
    }

    private OpenCommand parseOpenCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.TEXT_ONLY, lineNumber);
        return new OpenCommand(tokens.getFirst());
    }

    private ClickCommand parseClickCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.LOCATOR_ONLY, lineNumber);
        return new ClickCommand(LocatorParser.parse(tokens.get(0), tokens.get(1), lineNumber));
    }

    private TypeCommand parseTypeCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.LOCATOR_AND_TEXT, lineNumber);
        return new TypeCommand(
                LocatorParser.parse(tokens.get(0), tokens.get(1), lineNumber),
                tokens.get(2)
        );
    }

    private WaitCommand parseWaitCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.LOCATOR_AND_WAIT, lineNumber);
        String timeout = tokens.size() > 3 ? tokens.get(3) : "30";
        return new WaitCommand(
                LocatorParser.parse(tokens.get(0), tokens.get(1), lineNumber),
                WaitUtils.parseWaitType(tokens.get(2), lineNumber),
                timeout
        );
    }

    private AssertTextCommand parseAssertTextCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.LOCATOR_AND_TEXT, lineNumber);
        String text = tokens.get(2);
        if(tokens.get(2).startsWith("\"") && tokens.get(2).endsWith("\"")) {
            text = tokens.get(2).substring(1, tokens.get(2).length() - 1);
        }
        return new AssertTextCommand(LocatorParser.parse(tokens.get(0), tokens.get(1), lineNumber), text);
    }

    private AssertVisibleCommand parseAssertVisibleCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.LOCATOR_ONLY, lineNumber);
        return new AssertVisibleCommand(LocatorParser.parse(tokens.get(0), tokens.get(1), lineNumber));
    }

    private AssertURLCommand parseAssertURLCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.TEXT_ONLY, lineNumber);
        return new AssertURLCommand(tokens.getFirst());
    }


    // COMMANDS THAT USE NON-STANDARD ARG PARSING

    private LogCommand parseLogCommand(String args, int lineNumber) throws TestSyntaxException {
        String[] logArgs = LogUtils.parseLogArgs(args, lineNumber);
        return LogUtils.createLogCommand(logArgs, lineNumber);
    }

    private SetCommand parseSetCommand(String args, int lineNumber) throws TestSyntaxException {
        String[] setParts = args.split("=", 2);
        if (setParts.length != 2) {
            throw new TestSyntaxException("Invalid set command format. Expected: set <variable> = <value>", lineNumber);
        }
        String varName = setParts[0].trim();
        String value = StringUtils.stripQuotes(setParts[1].trim());
        return new SetCommand(varName, value);
    }
}
