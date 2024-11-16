/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: ForwardCommand.java
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

public class ForwardCommand implements Command {
    private static final Logger log = LogManager.getLogger(ForwardCommand.class);

    @Override
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        try {
            driver.navigate().forward();
            log.info("Navigated forward");
        } catch (WebDriverException e) {
            throw new CommandException("Failed to navigate forward in browser history");
        } catch (Exception e) {
            throw new CommandException("Unexpected error occurred during forward navigation");
        }
    }
}
