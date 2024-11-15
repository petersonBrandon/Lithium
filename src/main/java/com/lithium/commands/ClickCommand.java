/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: ClickCommand.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands;

import com.lithium.core.TestContext;
import com.lithium.locators.Locator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * The ClickCommand class represents a command to click on a web element located by a specific locator.
 * This command waits until the element is clickable before performing the click action.
 */
public class ClickCommand implements Command {
    private static final Logger log = LogManager.getLogger(ClickCommand.class);
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
        locator = new Locator(locator.getType(), context.resolveVariables(locator.getValue()));
        log.info("Clicking element: " + locator);
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator.toSeleniumBy()));
        element.click();
    }
}
