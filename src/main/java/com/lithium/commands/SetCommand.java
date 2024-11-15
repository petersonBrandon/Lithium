/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: SetCommand.java
 * Author: Brandon Peterson
 * Date: 11/14/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands;

import com.lithium.core.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * The SetCommand class represents a command to store a value in a variable during test execution.
 * This command allows for dynamic data storage that can be used by other commands.
 */
public class SetCommand implements Command {
    private static final Logger log = LogManager.getLogger(SetCommand.class);
    private final String variableName;
    private final String value;

    /**
     * Constructs a SetCommand with the specified variable name and value.
     *
     * @param variableName The name of the variable to store the value in.
     * @param value The value to store in the variable.
     */
    public SetCommand(String variableName, String value) {
        this.variableName = variableName;
        this.value = value;
    }

    /**
     * Executes the set command by storing the value in the specified variable.
     * Note: This implementation will need access to a variable storage mechanism.
     *
     * @param driver The WebDriver instance (unused in this command).
     * @param wait The WebDriverWait instance (unused in this command).
     */
    @Override
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        context.setVariable(variableName, value);
    }
}