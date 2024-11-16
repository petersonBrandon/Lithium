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
import com.lithium.exceptions.CommandException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger log = LogManager.getLogger(OpenTabCommand.class);
    private final String url;

    /**
     * Constructs an OpenTabCommand with the specified URL.
     *
     * @param url The URL to be opened in the new tab.
     */
    public OpenTabCommand(String url) {
        this.url = url;
    }

    /**
     * Executes the command to open a new tab and navigate to the specified URL.
     *
     * @param driver  The WebDriver instance used to control the browser.
     * @param wait    The WebDriverWait instance for handling timing.
     * @param context The TestContext instance for variable resolution.
     */
    @Override
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        String resolvedUrl = context.resolveVariables(url);
        log.info("Opening new tab and navigating to URL: {}", resolvedUrl);

        if (resolvedUrl == null || resolvedUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }

        try {
            // Validate URL format
            validateUrl(resolvedUrl);

            // Store the current window handle
            String originalWindow = driver.getWindowHandle();

            // Open new tab using JavaScript
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.open()");

            // Switch to the new tab (it will be the last window handle)
            ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
            String newTab = tabs.get(tabs.size() - 1);
            driver.switchTo().window(newTab);

            // Navigate to the URL
            driver.get(resolvedUrl);
            log.info("Successfully opened new tab and navigated to: {}", resolvedUrl);

        } catch (TimeoutException e) {
            String errorMsg = String.format("Timeout while loading URL in new tab: %s", resolvedUrl);
            throw new CommandException(errorMsg);

        } catch (WebDriverException e) {
            String errorMsg = String.format("Failed to open new tab or load URL: %s", resolvedUrl);
            throw new CommandException(errorMsg);

        } catch (IllegalArgumentException | CommandException e) {
            throw new CommandException(e.getMessage());

        } catch (Exception e) {
            String errorMsg = String.format("Unexpected error while opening new tab with URL: %s",
                    resolvedUrl);
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