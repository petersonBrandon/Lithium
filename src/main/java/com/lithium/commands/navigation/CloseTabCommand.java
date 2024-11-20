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
import com.lithium.util.logger.LithiumLogger;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

/**
 * The CloseTabCommand class represents a command to close a specified browser tab or the current tab.
 * This command can close a specific tab by its title/name or index, or the current tab if no specification is provided.
 */
public class CloseTabCommand implements Command {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private String windowIdentifier;
    private final int lineNumber;

    /**
     * Constructs a CloseTabCommand with no specific window identifier (closes current tab).
     */
    public CloseTabCommand(int lineNumber) {
        this(null, lineNumber);
    }

    /**
     * Constructs a CloseTabCommand with the specified window identifier.
     *
     * @param windowIdentifier The window name/title or index to close. If null, closes current tab.
     */
    public CloseTabCommand(String windowIdentifier, int lineNumber) {
        this.windowIdentifier = windowIdentifier;
        this.lineNumber = lineNumber;
    }

    /**
     * Executes the command to close the specified tab or current tab.
     *
     * @param driver  The WebDriver instance used to control the browser.
     * @param wait    The WebDriverWait instance for handling timing.
     * @param context The TestContext instance for variable resolution.
     */
    @Override
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        try {
            Set<String> windowHandles = driver.getWindowHandles();

            if (windowHandles.isEmpty()) {
                throw new CommandException(String.format("Line %s: No windows are currently open", lineNumber));
            }

            String currentHandle = driver.getWindowHandle();
            String handleToClose = currentHandle;

            // If a window identifier is specified, find the corresponding handle
            if (windowIdentifier != null) {
                windowIdentifier = context.resolveVariables(windowIdentifier);
                String resolvedIdentifier = context.resolveVariables(windowIdentifier);

                if (resolvedIdentifier.trim().isEmpty()) {
                    throw new IllegalArgumentException(String.format("Line %s: Window identifier cannot be empty", lineNumber));
                }

                ArrayList<String> handlesList = new ArrayList<>(windowHandles);

                // Try to parse as integer first (window index)
                try {
                    int index = Integer.parseInt(resolvedIdentifier) - 1;
                    if (index < 0 || index >= handlesList.size()) {
                        throw new CommandException(String.format(
                                "Line %s: Window index %d is out of range. Available windows: %d",
                                lineNumber, index + 1, handlesList.size()));
                    }
                    handleToClose = handlesList.get(index);
                } catch (NumberFormatException e) {
                    // Not an integer, try to find window by title
                    boolean windowFound = false;
                    for (String handle : handlesList) {
                        String originalHandle = driver.getWindowHandle();
                        driver.switchTo().window(handle);
                        if (Objects.equals(driver.getTitle(), resolvedIdentifier)) {
                            handleToClose = handle;
                            windowFound = true;
                            // Switch back to original window if we're not closing it
                            break;
                        }
                        driver.switchTo().window(originalHandle);
                    }
                    if (!windowFound) {
                        throw new CommandException(String.format(
                                "Line %s: No window found with title: %s", lineNumber, resolvedIdentifier));
                    }
                }
            }

            // Switch to the window we want to close if it's not the current one
            if (!handleToClose.equals(currentHandle)) {
                driver.switchTo().window(handleToClose);
            }

            // Close the window
            driver.close();
            if(windowIdentifier != null) {
                log.info(String.format("Successfully closed window %s", windowIdentifier));
            } else {
                log.info("Closed current window");
            }

            // Switch to the last remaining window if there are any
            Set<String> remainingHandles = driver.getWindowHandles();
            if (!remainingHandles.isEmpty()) {
                ArrayList<String> handlesList = new ArrayList<>(remainingHandles);
                String newHandle = handlesList.getLast();
                driver.switchTo().window(newHandle);
                log.info("Switched to remaining window");
            } else {
                log.info("No remaining windows open");
            }

        } catch (NoSuchWindowException e) {
            throw new CommandException(String.format("Line %s: Failed to switch windows: Window no longer exists", lineNumber));

        } catch (WebDriverException e) {
            throw new CommandException(String.format("Line %s: Failed to close window: %s", lineNumber,  e.getMessage()));

        } catch (IllegalArgumentException | CommandException e) {
            throw new CommandException(e.getMessage());

        } catch (Exception e) {
            throw new CommandException(String.format("Line %s: Unexpected error while closing window: %s", lineNumber, e.getMessage()));
        }
    }
}