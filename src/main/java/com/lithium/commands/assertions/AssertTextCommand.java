package com.lithium.commands.assertions;

import com.lithium.commands.Command;
import com.lithium.commands.WaitCommand;
import com.lithium.core.TestContext;
import com.lithium.exceptions.AssertionFailedException;
import com.lithium.locators.Locator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

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
        } catch (AssertionFailedException e) {
            throw new RuntimeException(e);
        }
    }
}
