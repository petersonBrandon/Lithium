/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: RefreshCommand.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands.navigation;

import com.lithium.commands.Command;
import com.lithium.core.ExecutionContext;
import com.lithium.exceptions.CommandException;
import com.lithium.util.logger.LithiumLogger;
import org.openqa.selenium.WebDriverException;

public class RefreshCommand implements Command {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private final int lineNumber;

    public RefreshCommand(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public void execute(ExecutionContext context) {
        try {
            context.getDriver().navigate().refresh();
            log.info("Refreshed page");
        } catch (WebDriverException e) {
            throw new CommandException(String.format("Line %s: Failed to refresh page", lineNumber));
        } catch (Exception e) {
            throw new CommandException(String.format("Line %s: Unexpected error occurred during page refresh", lineNumber));
        }
    }
}
