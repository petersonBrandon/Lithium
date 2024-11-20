/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: WaitParser.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.parser.commandTypes;

import com.lithium.commands.wait.WaitCommand;
import com.lithium.exceptions.TestSyntaxException;
import com.lithium.locators.LocatorParser;
import com.lithium.parser.utils.ArgPattern;
import com.lithium.parser.utils.CommandArgParser;
import com.lithium.parser.utils.WaitUtils;

import java.util.List;

/**
 * The WaitParser holds all the parsing logic for wait commands
 */
public class WaitParser {

    public static WaitCommand parseWaitCommand(String args, int lineNumber) throws TestSyntaxException {
        List<String> tokens = CommandArgParser.parseArgs(args, ArgPattern.LOCATOR_AND_WAIT, lineNumber);
        String timeout = tokens.size() > 3 ? tokens.get(3) : "30";
        return new WaitCommand(
                LocatorParser.parse(tokens.get(0), tokens.get(1), lineNumber),
                WaitUtils.parseWaitType(tokens.get(2), lineNumber),
                timeout,
                lineNumber
        );
    }
}
