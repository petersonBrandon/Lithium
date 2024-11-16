/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: UtilityParser.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.parser.commandTypes;

import com.lithium.commands.utility.LogCommand;
import com.lithium.commands.utility.data.SetCommand;
import com.lithium.exceptions.TestSyntaxException;
import com.lithium.parser.utils.LogUtils;
import com.lithium.parser.utils.StringUtils;

/**
 * The UtilityParser holds all the parsing logic for utility commands
 */
public class UtilityParser {

    public static LogCommand parseLogCommand(String args, int lineNumber) throws TestSyntaxException {
        String[] logArgs = LogUtils.parseLogArgs(args, lineNumber);
        return LogUtils.createLogCommand(logArgs, lineNumber);
    }

    public static SetCommand parseSetCommand(String args, int lineNumber) throws TestSyntaxException {
        String[] setParts = args.split("=", 2);
        if (setParts.length != 2) {
            throw new TestSyntaxException("Invalid set command format. Expected: set <variable> = <value>", lineNumber);
        }
        String varName = setParts[0].trim();
        String value = StringUtils.stripQuotes(setParts[1].trim());
        return new SetCommand(varName, value);
    }
}
