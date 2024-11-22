/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: ClickCommand.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
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
 * The ClickCommand class represents a command to click on a web element located by a specific locator.
 * This command waits until the element is clickable before performing the click action.
 */
public class ClickCommand implements Command {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private Locator locator;
    private final int lineNumber;

    /**
     * Constructs a ClickCommand with the specified Locator.
     *
     * @param locator The Locator used to find the element to be clicked.
     */
    public ClickCommand(Locator locator, int lineNumber) {
        this.locator = locator;
        this.lineNumber = lineNumber;
    }

    /**
     * Executes the click action on the web element identified by the locator, waiting until the element is clickable.
     *
     */
    @Override
    public void execute(ExecutionContext context) {
        try {
            WebElement element = context.getWait().until(ExpectedConditions.elementToBeClickable(locator.toSeleniumBy()));
            element.click();
            log.info(String.format("Clicked element: %s", locator));
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
                    "Line %s: Element click intercepted for: '%s %s'",
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
                    "Line %s: Unable to click element: '%s %s'",
                    lineNumber,
                    locator.getType(),
                    locator.getValue()
            ));
        }
    }
}
