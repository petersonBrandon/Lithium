/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: NavigationParser.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.parser.commandTypes;

import com.lithium.commands.navigation.BackCommand;
import com.lithium.commands.navigation.OpenCommand;
import com.lithium.exceptions.TestSyntaxException;
import com.lithium.parser.utils.ArgPattern;
import com.lithium.parser.utils.CommandArgParser;

import java.util.List;

/**
 * The NavigationParser holds all the parsing logic for navigation commands
 */
public class NavigationParser {

    public static OpenCommand parseOpenCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.TEXT_ONLY, lineNumber);
        return new OpenCommand(tokens.getFirst());
    }

    public static BackCommand parseBackCommand(String args, int lineNumber) throws TestSyntaxException {
        CommandArgParser.parseArgs(args, ArgPattern.NONE, lineNumber);
        return new BackCommand();
    }
}
