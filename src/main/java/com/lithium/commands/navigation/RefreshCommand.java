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
import com.lithium.core.TestContext;
import com.lithium.exceptions.CommandException;
import com.lithium.util.logger.LithiumLogger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RefreshCommand implements Command {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private final int lineNumber;

    public RefreshCommand(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        try {
            driver.navigate().refresh();
            log.info("Refreshed page");
        } catch (WebDriverException e) {
            throw new CommandException(String.format("Line %s: Failed to refresh page", lineNumber));
        } catch (Exception e) {
            throw new CommandException(String.format("Line %s: Unexpected error occurred during page refresh", lineNumber));
        }
    }
}
