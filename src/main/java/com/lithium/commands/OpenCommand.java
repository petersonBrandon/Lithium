/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: OpenCommand.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands;

import com.lithium.core.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

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
        log.info("Opening URL: " + context.resolveVariables(url));
        driver.get(context.resolveVariables(url));
    }
}
