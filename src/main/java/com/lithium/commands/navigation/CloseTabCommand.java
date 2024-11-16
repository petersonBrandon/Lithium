/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: CloseTabCommand.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands.navigation;

import com.lithium.commands.Command;
import com.lithium.core.TestContext;
import com.lithium.exceptions.CommandException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Set;

/**
 * The CloseTabCommand class represents a command to close the current browser tab.
 * This command closes the current tab and switches to the previous tab if available.
 */
public class CloseTabCommand implements Command {
    private static final Logger log = LogManager.getLogger(CloseTabCommand.class);

    /**
     * Executes the command to close the current tab and switch to the previous one.
     *
     * @param driver  The WebDriver instance used to control the browser.
     * @param wait    The WebDriverWait instance for handling timing.
     * @param context The TestContext instance for variable resolution.
     */
    @Override
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        log.info("Closing current tab");

        try {
            // Get all window handles before closing
            Set<String> windowHandles = driver.getWindowHandles();

            if (windowHandles.isEmpty()) {
                throw new CommandException("No windows are currently open");
            }

            if (windowHandles.size() == 1) {
                log.warn("Attempting to close the last remaining tab - this may close the browser");
            }

            // Store current window handle
            String currentHandle = driver.getWindowHandle();

            // Close current tab
            driver.close();
            log.info("Successfully closed current tab");

            // Switch to the previous tab if there are any remaining
            Set<String> remainingHandles = driver.getWindowHandles();
            if (!remainingHandles.isEmpty()) {
                // Convert to ArrayList for easier indexing
                ArrayList<String> handlesList = new ArrayList<>(remainingHandles);

                // Switch to the last tab in the list
                String newHandle = handlesList.get(handlesList.size() - 1);
                driver.switchTo().window(newHandle);
                log.info("Switched to previous tab");
            } else {
                log.info("No remaining tabs open");
            }

        } catch (NoSuchWindowException e) {
            throw new CommandException("Failed to switch windows: Window no longer exists");

        } catch (WebDriverException e) {
            throw new CommandException("Failed to close current tab: " + e.getMessage());

        } catch (Exception e) {
            throw new CommandException("Unexpected error while closing tab: " + e.getMessage());
        }
    }
}