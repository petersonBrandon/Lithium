/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: WaitUtils.java
 * Author: Brandon Peterson
 * Date: 11/14/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.parser.utils;

import com.lithium.commands.WaitCommand;
import com.lithium.exceptions.TestSyntaxException;

/**
 * Utility class for handling and parsing wait types in the Lithium Automation Framework.
 * This class simplifies the validation and conversion of wait type strings into their
 * corresponding enumerations used by the framework's wait commands.
 */
public class WaitUtils {

    /**
     * Parses a string representation of a wait type and converts it into a corresponding enum value.
     *
     * @param waitTypeStr the string representation of the wait type (e.g., "presence", "visible", "clickable")
     * @param lineNumber  the line number for error reporting
     * @return the corresponding {@link WaitCommand.WaitType} enum value
     * @throws TestSyntaxException if the wait type is invalid or not recognized
     */
    public static WaitCommand.WaitType parseWaitType(String waitTypeStr, int lineNumber) throws TestSyntaxException {
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
}