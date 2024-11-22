/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: Command.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands;

import com.lithium.core.TestRunner;

/**
 * The Command interface represents an action or operation to be performed in a Lithium test.
 * Implementations of this interface define specific commands (e.g., click, type, open) that
 * can be executed using a WebDriver instance.
 */
public interface Command {
    void execute(TestRunner.ExecutionContext context);
}
