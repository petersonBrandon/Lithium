/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: AssertionFailedException.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.exceptions;

/**
 * The AssertionFailedException class represents an exception that occurs when there is an assertion error
 * during a Lithium assertion command.
 */
public class AssertionFailedException extends RuntimeException {

    /**
     * Constructs an AssertionFailedException with a specified message
     *
     * @param message Message to send
     */
    public AssertionFailedException(String message) {
        super(message);
    }
}
