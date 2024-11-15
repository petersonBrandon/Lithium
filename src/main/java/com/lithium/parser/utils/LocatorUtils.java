/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: LocatorUtils.java
 * Author: Brandon Peterson
 * Date: 11/14/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.parser.utils;

import com.lithium.exceptions.TestSyntaxException;

/**
 * Utility class for parsing and handling locator-related arguments in the
 * Lithium Automation Framework. This class provides helper methods to extract
 * and validate locators and their associated arguments from test script lines.
 */
public class LocatorUtils {
    /**
     * Parses locator arguments for commands like 'click' and 'type'.
     *
     * @param args       the argument string containing the locator type and value
     * @param lineNumber the line number for error reporting
     * @return an array containing the locator type and locator value
     * @throws TestSyntaxException if the format is invalid
     */
    public static String[] parseLocatorArgs(String args, int lineNumber) throws TestSyntaxException {
        String[] parts = args.trim().split("\\s+", 2);
        if (parts.length != 2) {
            throw new TestSyntaxException("Invalid locator format. Expected: <type> \"value\"", lineNumber);
        }

        String locatorType = parts[0];
        String locatorValue = StringUtils.extractQuotedString(parts[1], lineNumber);

        return new String[]{locatorType, locatorValue};
    }

    /**
     * Parses arguments for the 'type' command, which includes locator type, locator value, and text to type.
     *
     * @param args       the argument string containing the locator and text
     * @param lineNumber the line number for error reporting
     * @return an array containing locator type, locator value, and text to type
     * @throws TestSyntaxException if the format is invalid
     */
    public static String[] parseTypeArgs(String args, int lineNumber) throws TestSyntaxException {
        String[] parts = args.trim().split("\\s+", 2);
        if (parts.length != 2) {
            throw new TestSyntaxException("Invalid type command format", lineNumber);
        }

        String locatorType = parts[0];
        String remaining = parts[1].trim();
        String locatorValue = StringUtils.extractQuotedString(remaining, lineNumber);

        int secondQuoteEnd = remaining.indexOf("\"", remaining.indexOf("\"") + 1) + 1;
        String textToType = StringUtils.extractQuotedString(remaining.substring(secondQuoteEnd).trim(), lineNumber);

        return new String[]{locatorType, locatorValue, textToType};
    }

    /**
     * Parses arguments for the 'wait' command, which includes locator type, locator value, wait type, and optional timeout.
     *
     * @param args       the argument string containing the locator and wait parameters
     * @param lineNumber the line number for error reporting
     * @return an array containing locator type, locator value, wait type, and optional timeout
     * @throws TestSyntaxException if the format is invalid
     */
    public static String[] parseWaitArgs(String args, int lineNumber) throws TestSyntaxException {
        String[] parts = args.trim().split("\\s+");
        if (parts.length < 3) {
            throw new TestSyntaxException(
                    "Invalid wait command format. Expected: <locator_type> \"locator\" <wait_type> [timeout]",
                    lineNumber
            );
        }

        return new String[]{
                parts[0],
                StringUtils.extractQuotedString(parts[1], lineNumber),
                parts[2],
                parts.length > 3 ? parts[3] : null
        };
    }

    /**
     * Parses arguments for the 'assertText' command, which includes locator type, locator value, and expected text.
     *
     * @param args       the argument string containing the locator and expected text
     * @param lineNumber the line number for error reporting
     * @return an array containing locator type, locator value, and expected text
     * @throws TestSyntaxException if the format is invalid
     */
    public static String[] parseAssertTextArgs(String args, int lineNumber) throws TestSyntaxException {
        String[] parts = args.trim().split("\\s+");
        if (parts.length != 3) {
            throw new TestSyntaxException(
                    "Invalid assertText command format. Expected: <locator_type> \"locator\" <expected_text>",
                    lineNumber
            );
        }

        return new String[]{
                parts[0],
                StringUtils.extractQuotedString(parts[1], lineNumber),
                parts[2],
        };
    }

    /**
     * Parses arguments for element assertions such as 'assertVisible', which include locator type and locator value.
     *
     * @param args       the argument string containing the locator
     * @param lineNumber the line number for error reporting
     * @return an array containing locator type and locator value
     * @throws TestSyntaxException if the format is invalid
     */
    public static String[] parseAssertElementArgs(String args, int lineNumber) throws TestSyntaxException {
        String[] parts = args.trim().split("\\s+");
        if (parts.length != 2) {
            throw new TestSyntaxException(
                    "Invalid assertVisible command format. Expected: <locator_type> \"locator\"",
                    lineNumber
            );
        }

        return new String[]{
                parts[0],
                StringUtils.extractQuotedString(parts[1], lineNumber),
        };
    }
}