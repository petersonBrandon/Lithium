/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: SwitchToWindowCommand.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands.navigation;

import com.lithium.commands.Command;
import com.lithium.core.TestContext;
import com.lithium.exceptions.CommandException;
import com.lithium.util.logger.LithiumLogger;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

/**
 * The SwitchToWindowCommand class represents a command to switch between browser windows.
 * This command can switch to a window either by its name/title or by index.
 */
public class SwitchToWindowCommand implements Command {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private String windowIdentifier;

    /**
     * Constructs a SwitchToWindowCommand with the specified window identifier.
     *
     * @param windowIdentifier The window name/title or index to switch to.
     */
    public SwitchToWindowCommand(String windowIdentifier) {
        this.windowIdentifier = windowIdentifier;
    }

    /**
     * Executes the window switch command, instructing the WebDriver to switch to the specified window.
     *
     * @param driver  The WebDriver instance used to switch windows.
     * @param wait    The WebDriverWait instance for handling timing.
     * @param context The TestContext instance for variable resolution.
     */
    @Override
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        windowIdentifier = context.resolveVariables(windowIdentifier);

        try {
            if (windowIdentifier == null || windowIdentifier.trim().isEmpty()) {
                throw new IllegalArgumentException("Window identifier cannot be null or empty");
            }

            Set<String> windowHandles = driver.getWindowHandles();
            if (windowHandles.isEmpty()) {
                throw new CommandException("No windows are currently open");
            }

            // Convert handles to list for index access
            ArrayList<String> handlesList = new ArrayList<>(windowHandles);

            // Try to parse as integer first (window index)
            try {
                int index = Integer.parseInt(windowIdentifier) - 1;
                if (index < 0 || index >= handlesList.size()) {
                    throw new CommandException(String.format(
                            "Window index %d is out of range. Available windows: %d",
                            index, handlesList.size()));
                }
                driver.switchTo().window(handlesList.get(index));
                log.info(String.format("Switched to window at index: %s", index));
                return;
            } catch (NumberFormatException e) {
                // Not an integer, try to find window by title/name
                boolean windowFound = false;
                for (String handle : handlesList) {
                    driver.switchTo().window(handle);
                    if (Objects.equals(driver.getTitle(), windowIdentifier)) {
                        windowFound = true;
                        log.info(String.format("Successfully switched to window with title: %s", windowIdentifier));
                        break;
                    }
                }
                if (!windowFound) {
                    throw new CommandException(String.format(
                            "No window found with title: %s", windowIdentifier));
                }
            }

        } catch (NoSuchWindowException e) {
            String errorMsg = String.format("Window not found: %s", windowIdentifier);
            throw new CommandException(errorMsg);

        } catch (TimeoutException e) {
            String errorMsg = String.format("Timeout while switching to window: %s", windowIdentifier);
            throw new CommandException(errorMsg);

        } catch (IllegalArgumentException | CommandException e) {
            throw new CommandException(e.getMessage());

        } catch (Exception e) {
            String errorMsg = String.format("Unexpected error while switching to window: %s",
                    windowIdentifier);
            throw new CommandException(errorMsg);
        }
    }
}