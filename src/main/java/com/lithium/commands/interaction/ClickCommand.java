/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: ClickCommand.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands.interaction;

import com.lithium.commands.Command;
import com.lithium.core.TestContext;
import com.lithium.exceptions.CommandException;
import com.lithium.locators.Locator;
import com.lithium.util.logger.LithiumLogger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * The ClickCommand class represents a command to click on a web element located by a specific locator.
 * This command waits until the element is clickable before performing the click action.
 */
public class ClickCommand implements Command {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private Locator locator;

    /**
     * Constructs a ClickCommand with the specified Locator.
     *
     * @param locator The Locator used to find the element to be clicked.
     */
    public ClickCommand(Locator locator) {
        this.locator = locator;
    }

    /**
     * Executes the click action on the web element identified by the locator, waiting until the element is clickable.
     *
     * @param driver The WebDriver instance used to interact with the web page.
     * @param wait   The WebDriverWait instance used to wait for the element to become clickable.
     */
    @Override
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        try {
            locator = new Locator(locator.getType(), context.resolveVariables(locator.getValue()));
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator.toSeleniumBy()));
            element.click();
            log.info(String.format("Clicked element: %s", locator));
        } catch (NoSuchElementException e) {
            throw new CommandException(String.format(
                    "Element not found: '%s %s'",
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (StaleElementReferenceException e) {
            throw new CommandException(String.format(
                    "Stale element reference for: '%s %s'",
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (ElementClickInterceptedException e) {
            throw new CommandException(String.format(
                    "Element click intercepted for: '%s %s'",
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (TimeoutException e) {
            throw new CommandException(String.format(
                    "Timeout waiting for clickable element: '%s %s'",
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (Exception e) {
            throw new CommandException(String.format(
                    "Unable to click element: '%s %s'",
                    locator.getType(),
                    locator.getValue()
            ));
        }
    }
}
