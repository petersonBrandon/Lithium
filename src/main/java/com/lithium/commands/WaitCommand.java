/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: WaitCommand.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands;

import com.lithium.locators.Locator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.logging.Logger;

/**
 * The WaitCommand class represents a command to wait for the presence of a web element located by a given locator.
 * This command pauses the execution until the specified element is found in the DOM.
 */
public class WaitCommand implements Command {
    private static final Logger LOGGER = Logger.getLogger(WaitCommand.class.getName());
    private final Locator locator;
    private final WaitType waitType;
    private final long timeoutSeconds;

    public enum WaitType {
        PRESENCE,
        VISIBLE,
        CLICKABLE
    }

    /**
     * Constructs a WaitCommand with the specified Locator.
     *
     * @param locator The Locator used to identify the element to wait for.
     */
    public WaitCommand(Locator locator, WaitType waitType, long timeoutSeconds) {
        this.locator = locator;
        this.waitType = waitType;
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Executes the wait action, pausing execution until the element specified by the locator is present in the DOM.
     *
     * @param driver The WebDriver instance used to interact with the web page.
     * @param wait   The WebDriverWait instance used to wait for the element to be present in the DOM.
     */
    @Override
    public void execute(WebDriver driver, WebDriverWait wait) {
        LOGGER.info("Waiting for element: " + locator + " (type: " + waitType + ", timeout: " + timeoutSeconds + "s)");

        switch (waitType) {
            case PRESENCE:
                wait.withTimeout(Duration.ofSeconds(timeoutSeconds))
                        .until(ExpectedConditions.presenceOfElementLocated(locator.toSeleniumBy()));
                break;
            case VISIBLE:
                wait.withTimeout(Duration.ofSeconds(timeoutSeconds))
                        .until(ExpectedConditions.visibilityOfElementLocated(locator.toSeleniumBy()));
                break;
            case CLICKABLE:
                wait.withTimeout(Duration.ofSeconds(timeoutSeconds))
                        .until(ExpectedConditions.elementToBeClickable(locator.toSeleniumBy()));
                break;
        }
    }
}
