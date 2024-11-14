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
    private final int lineNumber;

    /**
     * Constructs a new TestSyntaxException with a specified error message and line number.
     *
     * @param message    The detail message describing the syntax error.
     * @param lineNumber The line number in the test file where the syntax error occurred.
     */
    public TestSyntaxException(String message, int lineNumber) {
        super(String.format("Line %d: %s", lineNumber, message));
        this.lineNumber = lineNumber;
    }

    /**
     * Gets the line number where the syntax error occurred.
     *
     * @return The line number of the syntax error.
     */
    public int getLineNumber() {
        return lineNumber;
    }
}
