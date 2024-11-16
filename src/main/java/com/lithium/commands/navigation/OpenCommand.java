/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: OpenCommand.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands.navigation;

import com.lithium.commands.Command;
import com.lithium.core.TestContext;
import com.lithium.exceptions.CommandException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * The OpenCommand class represents a command to navigate to a specified URL in the browser.
 * This command instructs the WebDriver to load the given URL.
 */
public class OpenCommand implements Command {
    private static final Logger log = LogManager.getLogger(OpenCommand.class);
    private final String url;

    /**
     * Constructs an OpenCommand with the specified URL.
     *
     * @param url The URL to be opened in the browser.
     */
    public OpenCommand(String url) {
        this.url = url;
    }

    /**
     * Executes the navigation command, instructing the WebDriver to open the specified URL.
     *
     * @param driver The WebDriver instance used to open the URL.
     * @param wait   The WebDriverWait instance, not used in this command but provided for interface consistency.
     */
    @Override
    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        String resolvedUrl = context.resolveVariables(url);

        if (resolvedUrl == null || resolvedUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }

        try {
            // Validate URL format
            validateUrl(resolvedUrl);

            driver.get(resolvedUrl);
            log.info("Opened URL: {}", resolvedUrl);
        } catch (TimeoutException e) {
            String errorMsg = String.format("Timeout while loading URL: %s", resolvedUrl);
            throw new CommandException(errorMsg);

        } catch (WebDriverException e) {
            String errorMsg = String.format("Failed to load URL: %s",
                    resolvedUrl);
            throw new CommandException(errorMsg);

        } catch (IllegalArgumentException | CommandException e) {
            throw new CommandException(e.getMessage());

        } catch (Exception e) {
            String errorMsg = String.format("Unexpected error while opening URL: %s",
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
