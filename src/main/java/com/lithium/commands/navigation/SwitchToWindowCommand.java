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
import com.lithium.core.TestRunner;
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
    private final int lineNumber;

    /**
     * Constructs a SwitchToWindowCommand with the specified window identifier.
     *
     * @param windowIdentifier The window name/title or index to switch to.
     */
    public SwitchToWindowCommand(String windowIdentifier, int lineNumber) {
        this.windowIdentifier = windowIdentifier;
        this.lineNumber = lineNumber;
    }

    /**
     * Executes the window switch command, instructing the WebDriver to switch to the specified window.
     *
     * @param context The TestContext instance for variable resolution.
     */
    @Override
    public void execute(TestRunner.ExecutionContext context) {
        try {
            if (windowIdentifier == null || windowIdentifier.trim().isEmpty()) {
                throw new IllegalArgumentException(String.format("Line %s: Window identifier cannot be null or empty", lineNumber));
            }

            Set<String> windowHandles = context.getDriver().getWindowHandles();
            if (windowHandles.isEmpty()) {
                throw new CommandException(String.format("Line %s: No windows are currently open", lineNumber));
            }

            // Convert handles to list for index access
            ArrayList<String> handlesList = new ArrayList<>(windowHandles);

            // Try to parse as integer first (window index)
            try {
                int index = Integer.parseInt(windowIdentifier) - 1;
                if (index < 0 || index >= handlesList.size()) {
                    throw new CommandException(String.format(
                            "Line %s: Window index %d is out of range. Available windows: %d",
                            lineNumber, index, handlesList.size()));
                }
                context.getDriver().switchTo().window(handlesList.get(index));
                log.info(String.format("Switched to window at index: %s", index));
                return;
            } catch (NumberFormatException e) {
                // Not an integer, try to find window by title/name
                boolean windowFound = false;
                for (String handle : handlesList) {
                    context.getDriver().switchTo().window(handle);
                    if (Objects.equals(context.getDriver().getTitle(), windowIdentifier)) {
                        windowFound = true;
                        log.info(String.format("Successfully switched to window with title: %s", windowIdentifier));
                        break;
                    }
                }
                if (!windowFound) {
                    throw new CommandException(String.format(
                            "Line %s: No window found with title: %s", lineNumber, windowIdentifier));
                }
            }

        } catch (NoSuchWindowException e) {
            String errorMsg = String.format("Line %s: Window not found: %s", lineNumber, windowIdentifier);
            throw new CommandException(errorMsg);

        } catch (TimeoutException e) {
            String errorMsg = String.format("Line %s: Timeout while switching to window: %s", lineNumber, windowIdentifier);
            throw new CommandException(errorMsg);

        } catch (IllegalArgumentException | CommandException e) {
            throw new CommandException(e.getMessage());

        } catch (Exception e) {
            String errorMsg = String.format("Line %s: Unexpected error while switching to window: %s",
                    lineNumber, windowIdentifier);
            throw new CommandException(errorMsg);
        }
    }
}