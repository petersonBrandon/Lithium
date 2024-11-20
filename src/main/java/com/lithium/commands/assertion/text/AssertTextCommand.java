/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: AssertTextCommand.java
 * Author: Brandon Peterson
 * Date: 11/14/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands.assertion.text;

import com.lithium.commands.Command;
import com.lithium.core.TestContext;
import com.lithium.exceptions.AssertionFailedException;
import com.lithium.locators.Locator;
import com.lithium.util.logger.LithiumLogger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * The AssertTextCommand class implements the Command interface and provides
 * functionality to assert that the text of a specified web element matches
 * an expected value. This command supports variable resolution in locators
 * through the provided TestContext.
 */
public class AssertTextCommand implements Command {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private Locator locator;
    private final String expectedText;
    private final int lineNumber;

    /**
     * Constructs an AssertTextCommand instance with the specified locator
     * and expected text value.
     *
     * @param locator      the Locator object representing the element to assert
     * @param expectedText the expected text value for the element
     */
    public AssertTextCommand(Locator locator, String expectedText, int lineNumber) {
        this.locator = locator;
        this.expectedText = expectedText;
        this.lineNumber = lineNumber;
    }

    /**
     * Executes the text assertion command. Verifies that the text of the web
     * element identified by the locator matches the expected value.
     *
     * @param driver  the WebDriver instance for browser interaction
     * @param wait    the WebDriverWait instance for element wait conditions
     * @param context the TestContext for variable resolution in locators
     * @throws RuntimeException if an AssertionFailedException or other
     *                          exception occurs during execution
     */
    @Override
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        try {
            // Resolve dynamic variables in the locator
            locator = new Locator(locator.getType(), context.resolveVariables(locator.getValue()));

            // Locate the web element and assert its text
            WebElement element = wait.until(
                    ExpectedConditions.presenceOfElementLocated(locator.toSeleniumBy())
            );

            String actualText = element.getText().trim();
            if (!actualText.equals(expectedText)) {
                throw new AssertionFailedException(String.format(
                        "\nLine %s: Text assertion failed for element '%s'\nExpected: '%s'\nActual: '%s'",
                        lineNumber, locator.getValue(), expectedText, actualText
                ));
            }

            // Log successful assertion
            log.info(String.format("Asserted element '%s %s' text equals '%s'", locator.getType(), locator.getValue(), expectedText));
        } catch (AssertionFailedException e) {
            throw new RuntimeException(e);
        } catch (NoSuchElementException e) {
            throw new AssertionFailedException(String.format(
                    "Element with locator '%s %s' was not found",
                    locator.getType(), locator.getValue()
            ));
        } catch (Exception e) {
            throw new AssertionFailedException(String.format(
                    "Unable to assert text of element with locator '%s %s'",
                    locator.getType(), locator.getValue()
            ));
        }
    }
}