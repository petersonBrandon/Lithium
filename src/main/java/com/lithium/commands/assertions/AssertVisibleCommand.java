/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: AssertVisibleCommand.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands.assertions;

import com.lithium.commands.Command;
import com.lithium.core.TestContext;
import com.lithium.exceptions.AssertionFailedException;
import com.lithium.locators.Locator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AssertVisibleCommand implements Command {
    private static final Logger log = LogManager.getLogger(AssertVisibleCommand.class);
    private Locator locator;

    public AssertVisibleCommand(Locator locator) {
        this.locator = locator;
    }

    @Override
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        try {
            locator = new Locator(locator.getType(), context.resolveVariables(locator.getValue()));

            wait.until(ExpectedConditions.visibilityOfElementLocated(locator.toSeleniumBy()));
            log.info("Asserted element visible with '{} {}'", locator.getType(), locator.getValue());
        } catch (NoSuchElementException e) {
            throw new AssertionFailedException(String.format(
                    "Element not found: '%s %s'",
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (TimeoutException e) {
            throw new AssertionFailedException(String.format(
                    "Element not visible within timeout: '%s %s'",
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (Exception e) {
            throw new AssertionFailedException(String.format(
                    "Unable to assert element visibility: '%s %s'",
                    locator.getType(),
                    locator.getValue()
            ));
        }
    }
}
