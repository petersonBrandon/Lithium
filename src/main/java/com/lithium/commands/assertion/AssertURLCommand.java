/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: AssertURLCommand.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands.assertion;

import com.lithium.commands.Command;
import com.lithium.commands.assertion.text.AssertTextCommand;
import com.lithium.core.TestContext;
import com.lithium.exceptions.AssertionFailedException;
import com.lithium.exceptions.CommandException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * The AssertURLCommand handles the verification of the current URL
 * to an expected URL.
 */
public class AssertURLCommand implements Command {
    private static final Logger log = LogManager.getLogger(AssertTextCommand.class);
    private String expectedUrl;

    /**
     * Construct the AssertURLCommand with and expected URL
     *
     * @param expectedUrl URL where you expect to be
     */
    public AssertURLCommand(String expectedUrl) {
        this.expectedUrl = expectedUrl;
    }

    /**
     * Execute the URL assertion of current URL to expected.
     *
     * @param driver The WebDriver instance used to execute the command.
     * @param wait   The WebDriverWait instance used for managing wait conditions.
     * @param context The test context for resolving possible variables in the expected URL.
     */
    @Override
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        try {
            String currentUrl = driver.getCurrentUrl();
            expectedUrl = context.resolveVariables(expectedUrl);

            assert currentUrl != null;
            if(!currentUrl.equals(expectedUrl)) {
                throw new AssertionFailedException(String.format(
                   "Current URL %s does not match expected %s",
                   currentUrl,
                   expectedUrl
                ));
            }

            log.info("Asserted current URL equals {}", expectedUrl);
        } catch (AssertionFailedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new CommandException(String.format(
               "Unexpected error when asserting URL %s",
               expectedUrl
            ));
        }
    }
}
