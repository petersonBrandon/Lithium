/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: AssertVisibleCommand.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands.assertion.element;

import com.lithium.commands.Command;
import com.lithium.core.TestContext;
import com.lithium.core.TestRunner;
import com.lithium.exceptions.AssertionFailedException;
import com.lithium.locators.Locator;
import com.lithium.util.logger.LithiumLogger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * The AssertVisibleCommand class implements the Command interface and provides
 * functionality to assert that a specified web element is visible on the page.
 * This command leverages Selenium's visibility condition and supports variable
 * resolution for dynamic locators using the TestContext.
 */
public class AssertVisibleCommand implements Command {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private Locator locator;
    private final int lineNumber;

    /**
     * Constructs an AssertVisibleCommand instance with the specified locator.
     *
     * @param locator the Locator object representing the element to check visibility
     */
    public AssertVisibleCommand(Locator locator, int lineNumber) {
        this.locator = locator;
        this.lineNumber = lineNumber;
    }

    /**
     * Executes the visibility assertion command. Verifies that the web element
     * identified by the locator is visible on the page.
     *
     * @param context the TestContext for resolving dynamic variables in locators
     * @throws AssertionFailedException if the element is not visible or an error occurs
     */
    @Override
    public void execute(TestRunner.ExecutionContext context) {
        try {
            context.getWait().until(ExpectedConditions.visibilityOfElementLocated(locator.toSeleniumBy()));
            log.info(String.format("Asserted element visible with '%s %s'", locator.getType(), locator.getValue()));
        } catch (NoSuchElementException e) {
            throw new AssertionFailedException(String.format(
                    "Line %s: Element not found: '%s %s'",
                    lineNumber,
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (TimeoutException e) {
            throw new AssertionFailedException(String.format(
                    "Line %s: Element not visible within timeout: '%s %s'",
                    lineNumber,
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (Exception e) {
            throw new AssertionFailedException(String.format(
                    "Line %s: Unable to assert element visibility: '%s %s'",
                    lineNumber,
                    locator.getType(),
                    locator.getValue()
            ));
        }
    }
}
