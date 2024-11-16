/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: ArgPattern.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.parser.utils;

/**
 * Represents the different argument patterns that commands can have
 */
public enum ArgPattern {
    LOCATOR_ONLY(2),                // <type> "locator"
    LOCATOR_AND_TEXT(3),            // <type> "locator" "text"
    LOCATOR_AND_WAIT(3, 4),    // <type> "locator" <wait_type> [timeout]
    TEXT_ONLY(1),                   // "text"
    VARIABLE_AND_VALUE(2);          // <var> "value"

    private final int minArgs;
    private final int maxArgs;

    ArgPattern(int requiredArgs) {
        this(requiredArgs, requiredArgs);
    }

    ArgPattern(int minArgs, int maxArgs) {
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
    }

    public int getMinArgs() {
        return minArgs;
    }

    public int getMaxArgs() {
        return maxArgs;
    }
}
