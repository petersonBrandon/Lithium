/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: TypeCommand.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands.interaction;

import com.lithium.commands.Command;
import com.lithium.core.TestContext;
import com.lithium.exceptions.CommandException;
import com.lithium.locators.Locator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * The TypeCommand class represents a command to type a specified text into a web element located by a given locator.
 * This command waits until the element is clickable, clears any existing text, and then types the specified text.
 */
public class TypeCommand implements Command {
    private static final Logger log = LogManager.getLogger(TypeCommand.class);
    private Locator locator;
    private final String text;

    /**
     * Constructs a TypeCommand with the specified Locator and text.
     *
     * @param locator The Locator used to find the element to type into.
     * @param text    The text to be typed into the located element.
     */
    public TypeCommand(Locator locator, String text) {
        this.locator = locator;
        this.text = text;
    }

    /**
     * Executes the type action on the web element identified by the locator, waiting until the element is clickable.
     * Clears any existing text in the element and then sends the specified text.
     *
     * @param driver The WebDriver instance used to interact with the web page.
     * @param wait   The WebDriverWait instance used to wait for the element to become clickable.
     */
    @Override
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        try {
            // Resolve variables in locator and text
            locator = new Locator(locator.getType(), context.resolveVariables(locator.getValue()));
            String resolvedText = context.resolveVariables(text);

            log.info("Typing '{}' into element: {} {}",
                    resolvedText, locator.getType(), locator.getValue());

            // Wait for element and verify it's interactive
            WebElement element = waitForInteractiveElement(wait);

            // Clear existing text
            try {
                element.clear();
            } catch (InvalidElementStateException e) {
                log.warn("Unable to clear element before typing. Element might be read-only or not clearable. Proceeding with type operation.");
            }

            // Type the text
            element.sendKeys(resolvedText);

            // Verify the text was entered correctly
            verifyTextEntered(element, resolvedText);

        } catch (TimeoutException e) {
            String errorMsg = String.format(
                    "Timeout waiting for element to be clickable: %s %s",
                    locator.getType(), locator.getValue()
            );
            throw new CommandException(errorMsg);

        } catch (ElementNotInteractableException e) {
            String errorMsg = String.format(
                    "Element not interactable: %s %s",
                    locator.getType(), locator.getValue()
            );
            throw new CommandException(errorMsg);

        } catch (StaleElementReferenceException e) {
            String errorMsg = String.format(
                    "Element became stale: %s %s",
                    locator.getType(), locator.getValue()
            );
            throw new CommandException(errorMsg);

        } catch (Exception e) {
            String errorMsg = String.format(
                    "Failed to type text into element: %s %s",
                    locator.getType(), locator.getValue()
            );
            throw new CommandException(errorMsg);
        }
    }

    /**
     * Wait for element to be clickable
     *
     * @param wait Wait driver
     * @return the webElement
     */
    private WebElement waitForInteractiveElement(WebDriverWait wait) {
        try {
            return wait.until(ExpectedConditions.elementToBeClickable(locator.toSeleniumBy()));
        } catch (TimeoutException e) {
            throw new TimeoutException(String.format(
                    "Element not clickable within timeout: %s %s",
                    locator.getType(), locator.getValue()
            ));
        }
    }

    /**
     * Validate the text that was sent
     *
     * @param element Element that the text was sent to
     * @param expectedText Text that is to be verified
     */
    private void verifyTextEntered(WebElement element, String expectedText) {
        try {
            String actualValue = element.getAttribute("value");
            if (actualValue == null) {
                actualValue = element.getText();
            }

            if (!actualValue.contains(expectedText)) {
                log.warn("Typed text verification failed. Expected text '{}' not found in element. Actual text: '{}'",
                        expectedText, actualValue);
            }
        } catch (Exception e) {
            log.warn("Unable to verify typed text: {}", e.getMessage());
        }
    }
}
