/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: BackCommand.java
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

public class BackCommand implements Command {
    private static final LithiumLogger log = LithiumLogger.getInstance();

    @Override
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        try {
            driver.navigate().back();
            log.info("Navigated back");
        } catch (WebDriverException e) {
            throw new CommandException("Failed to navigate back in browser history");
        } catch (Exception e) {
            throw new CommandException("Unexpected error occurred during back navigation");
        }
    }
}
