/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: TestSyntaxException.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.exceptions;

/**
 * The TestSyntaxException class represents an exception that occurs when there is a syntax error in a Lithium test file.
 * It includes the line number where the syntax error occurred, which helps in identifying the source of the error.
 */
public class TestSyntaxException extends Exception {

    /**
     * Constructs a new TestSyntaxException with a specified error message and line number.
     *
     * @param message    The detail message describing the syntax error.
     */
    public TestSyntaxException(String message) {
        super(message);
    }
}
