/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: CommandException.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.exceptions;

public class CommandException extends RuntimeException {
    public CommandException(String message) {
        super(message);
    }
}
