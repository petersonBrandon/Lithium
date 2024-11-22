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
import com.lithium.core.TestRunner;
import com.lithium.exceptions.CommandException;
import com.lithium.util.logger.LithiumLogger;
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
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private final String url;
    private final int lineNumber;

    /**
     * Constructs an OpenCommand with the specified URL.
     *
     * @param url The URL to be opened in the browser.
     */
    public OpenCommand(String url, int lineNumber) {
        this.url = url;
        this.lineNumber = lineNumber;
    }

    /**
     * Executes the navigation command, instructing the WebDriver to open the specified URL.
     *
     */
    @Override
    public void execute(TestRunner.ExecutionContext context) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException(String.format("Line %s: URL cannot be null or empty", lineNumber));
        }

        try {
            // Validate URL format
            validateUrl(url);

            context.getDriver().get(url);
            log.info(String.format("Opened URL: %s", url));
        } catch (TimeoutException e) {
            String errorMsg = String.format("Line %s: Timeout while loading URL: %s", lineNumber, url);
            throw new CommandException(errorMsg);

        } catch (WebDriverException e) {
            String errorMsg = String.format("Line %s: Failed to load URL: %s",
                    lineNumber, url);
            throw new CommandException(errorMsg);

        } catch (IllegalArgumentException | CommandException e) {
            throw new CommandException(e.getMessage());

        } catch (Exception e) {
            String errorMsg = String.format("Line %s: Unexpected error while opening URL: %s",
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
                        String.format("Line %s: Unsupported protocol: %s. Only HTTP and HTTPS are supported.",
                                lineNumber, protocol)
                );
            }

        } catch (MalformedURLException | URISyntaxException e) {
            String errorMsg = String.format("Line %s: Invalid URL format: %s", lineNumber, urlString);
            throw new CommandException(errorMsg);
        }
    }
}
