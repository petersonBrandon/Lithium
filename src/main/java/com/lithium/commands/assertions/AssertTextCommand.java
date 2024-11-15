/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: AssertTextCommand.java
 * Author: Brandon Peterson
 * Date: 11/14/2024
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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AssertTextCommand implements Command {
    private static final Logger log = LogManager.getLogger(AssertTextCommand.class);
    private Locator locator;
    private String expectedText;

    public AssertTextCommand(Locator locator, String expectedText) {
        this.locator = locator;
        this.expectedText = expectedText;
    }

    @Override
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        try {
            locator = new Locator(locator.getType(), context.resolveVariables(locator.getValue()));

            WebElement element = wait.until(
                    ExpectedConditions.presenceOfElementLocated(locator.toSeleniumBy())
            );

            String actualText = element.getText().trim();
            if (!actualText.equals(expectedText)) {
                throw new AssertionFailedException(String.format(
                        "\nText assertion failed for element '%s'\nExpected: '%s'\nActual: '%s'",
                        locator.getValue(), expectedText, actualText
                ));
            }
            log.info("Asserted element '{} {}' text equals '{}'", locator.getType(), locator.getValue(), expectedText);
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
