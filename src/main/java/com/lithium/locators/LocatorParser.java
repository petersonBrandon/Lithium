/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: LocatorParser.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.locators;

import com.lithium.exceptions.TestSyntaxException;

/**
 * The LocatorParser class is responsible for parsing locator strings and converting them into Locator objects.
 * It validates the locator type and value, throwing a TestSyntaxException for any errors.
 */
public class LocatorParser {

    /**
     * Parses locator type and value into a Locator object.
     * If the type is unrecognized, a TestSyntaxException is thrown.
     *
     * @param type        The type of the locator (e.g., "css", "xpath", "id")
     * @param value       The value of the locator
     * @param lineNumber  The line number in the source file, used for error reporting
     * @return A Locator object representing the parsed locator type and value
     * @throws TestSyntaxException If the locator type is unrecognized
     */
    public static Locator parse(String type, String value, int lineNumber) throws TestSyntaxException {
        if (type == null || type.trim().isEmpty()) {
            throw new TestSyntaxException(
                    "Locator type cannot be empty. Valid types are: " +
                            String.join(", ", getValidTypesList())
            );
        }

        if (value == null || value.trim().isEmpty()) {
            throw new TestSyntaxException(
                    "Locator value cannot be empty"
            );
        }

        try {
            LocatorType locatorType = LocatorType.fromPrefix(type.trim().toLowerCase());
            return new Locator(locatorType, value.trim());
        } catch (IllegalArgumentException e) {
            throw new TestSyntaxException(
                    "Invalid locator type '" + type + "'. Valid types are: " +
                            String.join(", ", getValidTypesList())
            );
        }
    }

    /**
     * Backwards compatibility method for parsing a combined locator string.
     * This method is maintained for compatibility with existing code but delegates to the new parse method.
     *
     * @param locatorString The string representing the locator, e.g., "css \"#myElement\"".
     * @param lineNumber    The line number in the source file, used for error reporting.
     * @return A Locator object representing the parsed locator type and value.
     * @throws TestSyntaxException If the locator format is invalid or the locator type is unrecognized.
     * @deprecated Use {@link #parse(String, String, int)} instead
     */
    @Deprecated
    public static Locator parse(String locatorString, int lineNumber) throws TestSyntaxException {
        String[] parts = locatorString.trim().split("\\s+", 2);
        if (parts.length != 2) {
            throw new TestSyntaxException(
                    "Invalid locator format. Expected: <type> \"<value>\". Example: css \"#myElement\""
            );
        }

        String type = parts[0];
        String value = parts[1].trim();

        // Remove surrounding quotes if present
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }

        return parse(type, value, lineNumber);
    }

    /**
     * Gets a list of valid locator types as strings.
     *
     * @return An array of valid locator type prefixes.
     */
    private static String[] getValidTypesList() {
        return java.util.Arrays.stream(LocatorType.values())
                .map(type -> type.value)
                .toArray(String[]::new);
    }
}