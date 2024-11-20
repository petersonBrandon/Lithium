/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: InteractionParser.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.parser.commandTypes;

import com.lithium.commands.interaction.ClickCommand;
import com.lithium.commands.interaction.TypeCommand;
import com.lithium.exceptions.TestSyntaxException;
import com.lithium.locators.LocatorParser;
import com.lithium.parser.utils.ArgPattern;
import com.lithium.parser.utils.CommandArgParser;

import java.util.List;

/**
 * The InteractionParser holds all the parsing logic for interaction commands
 */
public class InteractionParser {

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
}
