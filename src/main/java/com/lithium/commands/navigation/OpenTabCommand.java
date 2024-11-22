/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: OpenTabCommand.java
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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

/**
 * The OpenTabCommand class represents a command to open a new browser tab and navigate to a specified URL.
 * This command opens a new tab, switches to it, and then loads the given URL.
 */
public class OpenTabCommand implements Command {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private String url;
    private final int lineNumber;

    /**
     * Constructs an OpenTabCommand with the specified URL.
     *
     * @param url The URL to be opened in the new tab.
     */
    public OpenTabCommand(String url, int lineNumber) {
        this.url = url;
        this.lineNumber = lineNumber;
    }

    /**
     * Executes the command to open a new tab and navigate to the specified URL.
     *
     * @param context The TestContext instance for variable resolution.
     */
    @Override
    public void execute(TestRunner.ExecutionContext context) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException(String.format("Line %s: URL cannot be null or empty", lineNumber));
        }

        try {
            // Validate URL format
            validateUrl(url);

            // Store the current window handle
            String originalWindow = context.getDriver().getWindowHandle();

            // Open new tab using JavaScript
            JavascriptExecutor js = (JavascriptExecutor) context.getDriver();
            js.executeScript("window.open()");

            // Switch to the new tab (it will be the last window handle)
            ArrayList<String> tabs = new ArrayList<>(context.getDriver().getWindowHandles());
            String newTab = tabs.getLast();
            context.getDriver().switchTo().window(newTab);

            // Navigate to the URL
            context.getDriver().get(url);
            log.info(String.format("Opened new tab and navigated to: %s", url));

        } catch (TimeoutException e) {
            String errorMsg = String.format("Line %s: Timeout while loading URL in new tab: %s", lineNumber, url);
            throw new CommandException(errorMsg);

        } catch (WebDriverException e) {
            String errorMsg = String.format("Line %s: Failed to open new tab or load URL: %s", lineNumber, url);
            throw new CommandException(errorMsg);

        } catch (IllegalArgumentException | CommandException e) {
            throw new CommandException(e.getMessage());

        } catch (Exception e) {
            String errorMsg = String.format("Line %s: Unexpected error while opening new tab with URL: %s",
                    lineNumber, url);
            throw new CommandException(errorMsg);
        }
    }

    /**
     * Validates URL string
     * @param urlString url to be validated
     * @throws CommandException exception error
     */
    private void validateUrl(String urlString) throws CommandException {
        try {
            URL url = new URL(urlString);
            // Additional validation by attempting to create URI
            url.toURI();

            // Check for supported protocols
            String protocol = url.getProtocol().toLowerCase();
            if (!protocol.equals("http") && !protocol.equals("https")) {
                throw new CommandException(
                        String.format("Unsupported protocol: %s. Only HTTP and HTTPS are supported.",
                                protocol)
                );
            }

        } catch (MalformedURLException | URISyntaxException e) {
            String errorMsg = String.format("Invalid URL format: %s", urlString);
            throw new CommandException(errorMsg);
        }
    }
}