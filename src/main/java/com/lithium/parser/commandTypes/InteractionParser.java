/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: InteractionParser.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.parser.commandTypes;

import com.lithium.commands.interaction.advanced.HoverCommand;
import com.lithium.commands.interaction.advanced.SelectCommand;
import com.lithium.commands.interaction.basic.*;
import com.lithium.exceptions.TestSyntaxException;
import com.lithium.locators.LocatorParser;
import com.lithium.parser.utils.ArgPattern;
import com.lithium.parser.utils.CommandArgParser;
import com.lithium.util.logger.LithiumLogger;

import java.util.List;

/**
 * The InteractionParser holds all the parsing logic for interaction commands
 */
public class InteractionParser {
    private static final LithiumLogger log = LithiumLogger.getInstance();

    public static ClickCommand parseClickCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.LOCATOR_ONLY, lineNumber);
        return new ClickCommand(LocatorParser.parse(tokens.get(0), tokens.get(1), lineNumber), lineNumber);
    }

    public static TypeCommand parseTypeCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.LOCATOR_AND_TEXT, lineNumber);
        return new TypeCommand(
                LocatorParser.parse(tokens.get(0), tokens.get(1), lineNumber),
                tokens.get(2),
                lineNumber
        );
    }

    public static ClearCommand parseClearCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.LOCATOR_ONLY, lineNumber);
        return new ClearCommand(LocatorParser.parse(tokens.get(0), tokens.get(1), lineNumber), lineNumber);
    }

    public static DoubleClickCommand parseDoubleClickCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.LOCATOR_ONLY, lineNumber);
        return new DoubleClickCommand(LocatorParser.parse(tokens.get(0), tokens.get(1), lineNumber), lineNumber);
    }

    public static RightClickCommand parseRightClickCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.LOCATOR_ONLY, lineNumber);
        return new RightClickCommand(LocatorParser.parse(tokens.get(0), tokens.get(1), lineNumber), lineNumber);
    }

    public static HoverCommand parseHoverCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.LOCATOR_ONLY, lineNumber);
        return new HoverCommand(LocatorParser.parse(tokens.get(0), tokens.get(1), lineNumber), lineNumber);
    }

    public static SelectCommand parseSelectCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.LOCATOR_AND_TYPE_WITH_VALUE, lineNumber);
        if(SelectCommand.SelectionType.valueOf(tokens.get(2).toUpperCase()).equals(SelectCommand.SelectionType.INDEX)) {
            return new SelectCommand(
                    LocatorParser.parse(tokens.get(0), tokens.get(1), lineNumber),
                    Integer.parseInt(tokens.get(3)),
                    lineNumber
            );
        } else {
            return new SelectCommand(
                    LocatorParser.parse(tokens.get(0), tokens.get(1), lineNumber),
                    tokens.get(3),
                    SelectCommand.SelectionType.valueOf(tokens.get(2).toUpperCase()),
                    lineNumber
            );
        }
    }
}
