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
import com.lithium.core.TestRunner;
import com.lithium.exceptions.CommandException;
import com.lithium.util.logger.LithiumLogger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ForwardCommand implements Command {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private final int lineNumber;

    public ForwardCommand(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public void execute(TestRunner.ExecutionContext context) {
        try {
            context.getDriver().navigate().forward();
            log.info("Navigated forward");
        } catch (WebDriverException e) {
            throw new CommandException(String.format("Line %s: Failed to navigate forward in browser history", lineNumber));
        } catch (Exception e) {
            throw new CommandException(String.format("Line %s: Unexpected error occurred during forward navigation", lineNumber));
        }
    }
}
