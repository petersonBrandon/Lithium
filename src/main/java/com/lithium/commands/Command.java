/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: Command.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands;

import com.lithium.core.TestContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * The Command interface represents an action or operation to be performed in a Lithium test.
 * Implementations of this interface define specific commands (e.g., click, type, open) that
 * can be executed using a WebDriver instance.
 */
public interface Command {

    /**
     * Executes the command using the provided WebDriver and WebDriverWait instances.
     *
     * @param driver The WebDriver instance used to execute the command.
     * @param wait   The WebDriverWait instance used for managing wait conditions.
     */
    void execute(WebDriver driver, WebDriverWait wait, TestContext context);
}
