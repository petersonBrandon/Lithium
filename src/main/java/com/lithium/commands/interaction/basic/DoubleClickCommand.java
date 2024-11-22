/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: DoubleClickCommand.java
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
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * The DoubleClickCommand class represents a command to perform a double-click action on a web element
 * located by a specific locator. This command waits until the element is clickable before performing
 * the double-click action.
 */
public class DoubleClickCommand implements Command {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private Locator locator;
    private final int lineNumber;

    /**
     * Constructs a DoubleClickCommand with the specified Locator.
     *
     * @param locator The Locator used to find the element to be double-clicked.
     * @param lineNumber The line number where this command appears in the test script.
     */
    public DoubleClickCommand(Locator locator, int lineNumber) {
        this.locator = locator;
        this.lineNumber = lineNumber;
    }

    /**
     * Executes the double-click action on the web element identified by the locator,
     * waiting until the element is clickable.
     * @param context The TestContext instance containing test execution context.
     */
    @Override
    public void execute(ExecutionContext context) {
        try {
            WebElement element = context.getWait().until(ExpectedConditions.elementToBeClickable(locator.toSeleniumBy()));

            Actions actions = new Actions(context.getDriver());
            actions.doubleClick(element).perform();

            log.info(String.format("Double-clicked element: %s", locator));
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
        } catch (ElementClickInterceptedException e) {
            throw new CommandException(String.format(
                    "Line %s: Element double-click intercepted for: '%s %s'",
                    lineNumber,
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (TimeoutException e) {
            throw new CommandException(String.format(
                    "Line %s: Timeout waiting for clickable element: '%s %s'",
                    lineNumber,
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (Exception e) {
            throw new CommandException(String.format(
                    "Line %s: Unable to double-click element: '%s %s'",
                    lineNumber,
                    locator.getType(),
                    locator.getValue()
            ));
        }
    }
}