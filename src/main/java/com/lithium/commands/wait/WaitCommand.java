/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: WaitCommand.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands.wait;

import com.lithium.commands.Command;
import com.lithium.core.TestContext;
import com.lithium.exceptions.CommandException;
import com.lithium.locators.Locator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * The WaitCommand class represents a command to wait for the presence of a web element located by a given locator.
 * This command pauses the execution until the specified element is found in the DOM.
 */
public class WaitCommand implements Command {
    private static final Logger log = LogManager.getLogger(WaitCommand.class);
    private static final long MAX_TIMEOUT = 300;
    private Locator locator;
    private final WaitType waitType;
    private final String timeoutSeconds;

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
    public WaitCommand(Locator locator, WaitType waitType, String timeoutSeconds) {
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
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        try {
            // Resolve variables
            locator = new Locator(locator.getType(), context.resolveVariables(locator.getValue()));
            long timeout = validateAndParseTimeout(context.resolveVariables(timeoutSeconds));

            log.info("Waiting for element to be {}: {} {} (timeout: {}s)",
                    waitType, locator.getType(), locator.getValue(), timeout);

            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            waitForElement(customWait);

        } catch (TimeoutException e) {
            String errorMsg = String.format(
                    "Timeout waiting for element to be %s: %s %s",
                    waitType, locator.getType(), locator.getValue()
            );
            throw new CommandException(errorMsg);

        } catch (InvalidSelectorException e) {
            String errorMsg = String.format(
                    "Invalid selector for element: %s %s",
                    locator.getType(), locator.getValue()
            );
            throw new CommandException(errorMsg);

        } catch (NumberFormatException e) {
            String errorMsg = String.format(
                    "Invalid timeout value: %s",
                    timeoutSeconds
            );
            throw new CommandException(errorMsg);

        } catch (Exception e) {
            String errorMsg = String.format(
                    "Unexpected error while waiting for element: %s %s",
                    locator.getType(), locator.getValue()
            );
            throw new CommandException(errorMsg);
        }
    }

    /**
     * Validate timeout value
     *
     * @param timeoutStr The timeout string to be validated
     * @return A parsed timout value
     */
    private long validateAndParseTimeout(String timeoutStr) {
        try {
            long timeout = Long.parseLong(timeoutStr);
            if (timeout <= 0) {
                throw new IllegalArgumentException("Timeout must be greater than 0");
            }
            if (timeout > MAX_TIMEOUT) {
                log.warn("Specified timeout {}s exceeds maximum allowed ({}s). Using maximum timeout.",
                        timeout, MAX_TIMEOUT);
                return MAX_TIMEOUT;
            }
            return timeout;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid timeout format: " + timeoutStr);
        }
    }

    /**
     * Perform actual wait action based on wait type
     *
     * @param wait wait driver
     */
    private void waitForElement(WebDriverWait wait) {
        switch (waitType) {
            case PRESENCE:
                wait.until(ExpectedConditions.presenceOfElementLocated(locator.toSeleniumBy()));
                break;
            case VISIBLE:
                wait.until(ExpectedConditions.visibilityOfElementLocated(locator.toSeleniumBy()));
                break;
            case CLICKABLE:
                wait.until(ExpectedConditions.elementToBeClickable(locator.toSeleniumBy()));
                break;
            default:
                throw new IllegalStateException("Unexpected wait type: " + waitType);
        }
    }
}
