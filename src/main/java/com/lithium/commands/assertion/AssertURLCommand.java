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
import com.lithium.core.TestContext;
import com.lithium.core.TestRunner;
import com.lithium.exceptions.AssertionFailedException;
import com.lithium.exceptions.CommandException;
import com.lithium.util.logger.LithiumLogger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * The AssertURLCommand handles the verification of the current URL
 * to an expected URL.
 */
public class AssertURLCommand implements Command {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private String expectedUrl;
    private final int lineNumber;

    /**
     * Construct the AssertURLCommand with and expected URL
     *
     * @param expectedUrl URL where you expect to be
     */
    public AssertURLCommand(String expectedUrl, int lineNumber) {
        this.expectedUrl = expectedUrl;
        this.lineNumber = lineNumber;
    }

    /**
     * Execute the URL assertion of current URL to expected.
     *
     * @param context The test context for resolving possible variables in the expected URL.
     */
    @Override
    public void execute(TestRunner.ExecutionContext context) {
        try {
            String currentUrl = context.getDriver().getCurrentUrl();

            assert currentUrl != null;
            if(!currentUrl.equals(expectedUrl)) {
                throw new AssertionFailedException(String.format(
                   "Line %s: Current URL '%s' does not match expected '%s'",
                   lineNumber,
                   currentUrl,
                   expectedUrl
                ));
            }

            log.info(String.format("Asserted current URL equals '%s'", expectedUrl));
        } catch (AssertionFailedException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            throw new CommandException(String.format(
               "Line %s: Unexpected error when asserting URL '%s'",
               lineNumber,
               expectedUrl
            ));
        }
    }
}
