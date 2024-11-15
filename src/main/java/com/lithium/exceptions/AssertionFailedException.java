/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: AssertionFailedException.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.exceptions;

public class AssertionFailedException extends RuntimeException {
    public AssertionFailedException(String message) {
        super(message);
    }
}
