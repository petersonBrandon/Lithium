/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: StringUtils.java
 * Author: Brandon Peterson
 * Date: 11/14/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.parser.utils;

import com.lithium.exceptions.TestSyntaxException;

/**
 * Utility class for string manipulation in the Lithium Automation Framework.
 * This class provides methods for handling quoted strings and basic string
 * operations that are commonly used in the framework's parsing logic.
 */
public class StringUtils {
    /**
     * Extracts the content enclosed within the first pair of double quotes in the input string.
     *
     * @param input      the input string containing quoted content
     * @param lineNumber the line number for error reporting
     * @return the string inside the quotes
     * @throws TestSyntaxException if no matching pair of quotes is found
     */
    public static String extractQuotedString(String input, int lineNumber) throws TestSyntaxException {
        StringBuilder result = new StringBuilder();
        int currentPos = 0;

        while (currentPos < input.length()) {
            // Find next quoted string
            int start = input.indexOf("\"", currentPos);
            int end = start != -1 ? input.indexOf("\"", start + 1) : -1;

            if (start == -1 || end == -1) {
                throw new TestSyntaxException("Missing quotes in command", lineNumber);
            }

            // Add the quoted string to result
            result.append(input.substring(start + 1, end));

            // Move position after the closing quote
            currentPos = end + 1;

            // Look for + operator
            while (currentPos < input.length() && Character.isWhitespace(input.charAt(currentPos))) {
                currentPos++;
            }

            if (currentPos < input.length() && input.charAt(currentPos) == '+') {
                currentPos++; // Skip the +
                // Skip whitespace after +
                while (currentPos < input.length() && Character.isWhitespace(input.charAt(currentPos))) {
                    currentPos++;
                }
            } else {
                break; // No more concatenations
            }
        }

        return result.toString();
    }

    /**
     * Removes surrounding double quotes from a string if both the first and last characters are quotes.
     *
     * @param value the string from which quotes should be removed
     * @return the unquoted string, or the original string if no quotes are found
     */
    public static String stripQuotes(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}