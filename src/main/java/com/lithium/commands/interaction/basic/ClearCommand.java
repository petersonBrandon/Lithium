/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: ClearCommand.java
 * Author: Brandon Peterson
 * Date: 11/20/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands.interaction.basic;

import com.lithium.commands.Command;
import com.lithium.core.ExecutionContext;
import com.lithium.exceptions.CommandException;
import com.lithium.locators.Locator;
import com.lithium.util.logger.LithiumLogger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * The ClearCommand class represents a command to clear the contents of a web element located by a specific locator.
 * This command waits until the element is visible and enabled before performing the clear action.
 */
public class ClearCommand implements Command {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private Locator locator;
    private final int lineNumber;

    /**
     * Constructs a ClearCommand with the specified Locator.
     *
     * @param locator The Locator used to find the element to be cleared.
     * @param lineNumber The line number where this command appears in the test script.
     */
    public ClearCommand(Locator locator, int lineNumber) {
        this.locator = locator;
        this.lineNumber = lineNumber;
    }

    /**
     * Executes the clear action on the web element identified by the locator.
     *
     * @param context The TestContext instance containing test execution context.
     */
    @Override
    public void execute(ExecutionContext context) {
        try {
            WebElement element = context.getWait().until(ExpectedConditions.elementToBeClickable(locator.toSeleniumBy()));
            element.clear();
            log.info(String.format("Cleared element: %s", locator));
        } catch (NoSuchElementException e) {
            throw new CommandException(String.format(
                    "Line %s: Element not found: '%s %s'",
                    lineNumber,
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (StaleElementReferenceException e) {
            throw new CommandException(String.format(
                    "Line %s: Stale element reference for: '%s %s'",
                    lineNumber,
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (TimeoutException e) {
            throw new CommandException(String.format(
                    "Line %s: Timeout waiting for element to be interactive: '%s %s'",
                    lineNumber,
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (Exception e) {
            throw new CommandException(String.format(
                    "Line %s: Unable to clear element: '%s %s'",
                    lineNumber,
                    locator.getType(),
                    locator.getValue()
            ));
        }
    }
}