/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: HoverCommand.java
 * Author: Brandon Peterson
 * Date: 11/20/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands.interaction.advanced;

import com.lithium.commands.Command;
import com.lithium.core.TestContext;
import com.lithium.core.TestRunner;
import com.lithium.exceptions.CommandException;
import com.lithium.locators.Locator;
import com.lithium.util.logger.LithiumLogger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * The HoverCommand class represents a command to perform a hover (mouse over) action 
 * on a web element located by a specific locator. This command waits until the element 
 * is visible before performing the hover action.
 */
public class HoverCommand implements Command {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private Locator locator;
    private final int lineNumber;

    /**
     * Constructs a HoverCommand with the specified Locator.
     *
     * @param locator The Locator used to find the element to hover over.
     * @param lineNumber The line number where this command appears in the test script.
     */
    public HoverCommand(Locator locator, int lineNumber) {
        this.locator = locator;
        this.lineNumber = lineNumber;
    }

    /**
     * Executes the hover action on the web element identified by the locator,
     * waiting until the element is visible.
     *
     * @param context The TestContext instance containing test execution context.
     */
    @Override
    public void execute(TestRunner.ExecutionContext context) {
        try {
            WebElement element = context.getWait().until(ExpectedConditions.elementToBeClickable(locator.toSeleniumBy()));

            Actions actions = new Actions(context.getDriver());
            actions.moveToElement(element).perform();

            log.info(String.format("Hovered over element: %s", locator));
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
        } catch (MoveTargetOutOfBoundsException e) {
            throw new CommandException(String.format(
                    "Line %s: Element not in viewport or cannot be hovered: '%s %s'",
                    lineNumber,
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (TimeoutException e) {
            throw new CommandException(String.format(
                    "Line %s: Timeout waiting for visible element: '%s %s'",
                    lineNumber,
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (Exception e) {
            throw new CommandException(String.format(
                    "Line %s: Unable to hover over element: '%s %s'",
                    lineNumber,
                    locator.getType(),
                    locator.getValue()
            ));
        }
    }
}