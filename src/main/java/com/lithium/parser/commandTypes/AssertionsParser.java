/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: AssertionsParser.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.parser.commandTypes;

import com.lithium.commands.assertion.AssertURLCommand;
import com.lithium.commands.assertion.element.AssertVisibleCommand;
import com.lithium.commands.assertion.text.AssertTextCommand;
import com.lithium.exceptions.TestSyntaxException;
import com.lithium.locators.LocatorParser;
import com.lithium.parser.utils.ArgPattern;
import com.lithium.parser.utils.CommandArgParser;

import java.util.List;

/**
 * The AssertionsParser holds all the parsing logic for assertion commands
 */
public class AssertionsParser {

    public static AssertTextCommand parseAssertTextCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.LOCATOR_AND_TEXT, lineNumber);
        String text = tokens.get(2);
        if(tokens.get(2).startsWith("\"") && tokens.get(2).endsWith("\"")) {
            text = tokens.get(2).substring(1, tokens.get(2).length() - 1);
        }
        return new AssertTextCommand(LocatorParser.parse(tokens.get(0), tokens.get(1), lineNumber), text, lineNumber);
    }

    public static AssertVisibleCommand parseAssertVisibleCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.LOCATOR_ONLY, lineNumber);
        return new AssertVisibleCommand(LocatorParser.parse(tokens.get(0), tokens.get(1), lineNumber), lineNumber);
    }

    public static AssertURLCommand parseAssertURLCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.TEXT_ONLY, lineNumber);
        return new AssertURLCommand(tokens.getFirst(), lineNumber);
    }
}
