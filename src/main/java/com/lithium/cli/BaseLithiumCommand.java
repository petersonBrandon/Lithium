/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: BaseLithiumCommand.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.cli;

/**
 * Abstract base command with common functionality
 */
public abstract class BaseLithiumCommand implements LithiumCommand {
    /**
     * Validate args are at the given minimum length
     *
     * @param args    list of args given
     * @param minimum expected minimum number of args
     */
    protected void validateArgsLength(String[] args, int minimum) {
        if (args.length < minimum) {
            throw new IllegalArgumentException("Insufficient arguments. Usage: " + getUsage());
        }
    }
}
