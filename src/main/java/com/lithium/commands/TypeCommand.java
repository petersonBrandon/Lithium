/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: TypeCommand.java
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
        locator = new Locator(locator.getType(), context.resolveVariables(locator.getValue()));
        log.info(String.format("Typing '%s' into element: %s", text, locator));
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator.toSeleniumBy()));
        element.clear();
        element.sendKeys(context.resolveVariables(text));
    }
}
