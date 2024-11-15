package com.lithium.parser.utils;

import com.lithium.commands.WaitCommand;
import com.lithium.exceptions.TestSyntaxException;

public class WaitUtils {
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