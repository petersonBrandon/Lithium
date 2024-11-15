/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: CommandException.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.exceptions;

/**
 * The CommandException class represents an exception that occurs when there is a command error in executing a Lithium command.
 */
public class CommandException extends RuntimeException {

    /**
     * Constructs a new CommandException with a specified message
     *
     * @param message Message to send
     */
    public CommandException(String message) {
        super(message);
    }
}
