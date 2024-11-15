package com.lithium.parser.utils;

import com.lithium.exceptions.TestSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocatorUtils {
    private static final Logger log = LogManager.getLogger(LocatorUtils.class);

    public static String[] parseLocatorArgs(String args, int lineNumber) throws TestSyntaxException {
        String[] parts = args.trim().split("\\s+", 2);
        if (parts.length != 2) {
            throw new TestSyntaxException("Invalid locator format. Expected: <type> \"value\"", lineNumber);
        }

        String locatorType = parts[0];
        String locatorValue = StringUtils.extractQuotedString(parts[1], lineNumber);

        return new String[]{locatorType, locatorValue};
    }

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

    public static String[] parseAssertTextArgs(String args, int lineNumber) throws TestSyntaxException {
        String[] parts = args.trim().split("\\s+");
        if (parts.length != 3) {
            throw new TestSyntaxException(
                    "Invalid assertText command format. Expected: <locator_type> \"locator\" <wait_type>",
                    lineNumber
            );
        }

        return new String[]{
                parts[0],
                StringUtils.extractQuotedString(parts[1], lineNumber),
                parts[2],
        };
    }

    public static String[] parseAssertElementArgs(String args, int lineNumber) throws TestSyntaxException {
        String[] parts = args.trim().split("\\s+");
        if (parts.length != 2) {
            throw new TestSyntaxException(
                    "Invalid assertText command format. Expected: <locator_type> \"locator\"",
                    lineNumber
            );
        }

        return new String[]{
                parts[0],
                StringUtils.extractQuotedString(parts[1], lineNumber),
        };
    }
}