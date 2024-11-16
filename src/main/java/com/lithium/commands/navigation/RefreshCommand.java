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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RefreshCommand implements Command {
    private static final Logger log = LogManager.getLogger(RefreshCommand.class);

    @Override
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        try {
            driver.navigate().refresh();
            log.info("Refreshed page");
        } catch (WebDriverException e) {
            throw new CommandException("Failed to refresh page");
        } catch (Exception e) {
            throw new CommandException("Unexpected error occurred during page refresh");
        }
    }
}
