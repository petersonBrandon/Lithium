package com.lithium.parser.utils;

import com.lithium.exceptions.TestSyntaxException;

public class StringUtils {
    public static String extractQuotedString(String input, int lineNumber) throws TestSyntaxException {
        int start = input.indexOf("\"");
        int end = input.indexOf("\"", start + 1);
        if (start == -1 || end == -1) {
            throw new TestSyntaxException("Missing quotes in command", lineNumber);
        }
        return input.substring(start + 1, end);
    }

    public static String stripQuotes(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}