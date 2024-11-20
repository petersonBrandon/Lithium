/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: TestRunner.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.core;

import com.lithium.commands.Command;
import com.lithium.exceptions.CommandException;
import com.lithium.util.LithiumLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * The TestRunner class is responsible for managing the WebDriver session and executing a series of commands
 * associated with a test case. It initializes the WebDriver in either headless or maximized mode,
 * depending on the specified options.
 */
public class TestRunner {
    private static final LithiumLogger log = LithiumLogger.getInstance();

    private final WebDriver driver;
    private final int timeout;
    private final String baseUrl;

    /**
     * Constructs a TestRunner with specified options for headless and maximized browser settings.
     *
     * @param headless   If true, the WebDriver will run in headless mode (no UI display).
     * @param maximized  If true, the WebDriver will start in maximized window mode.
     * @param browser    The browser to use for the test run.
     * @param timeout    The timeout in seconds for various WebDriver operations.
     * @param baseUrl    The base URL to initialize the browser with.
     */
    public TestRunner(boolean headless, boolean maximized, String browser, int timeout, String baseUrl) {
        this.timeout = timeout;
        this.baseUrl = baseUrl;

        BrowserDriverManager browserManager = new BrowserDriverManager();
        this.driver = browserManager.createDriver(browser, headless, maximized);

        // Set default timeouts
        this.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeout));
        this.driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeout));
        this.driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(timeout));

        // Navigate to the base URL if one is provided
        if (baseUrl != null && !baseUrl.isEmpty()) {
            log.info(String.format("Navigating to base URL: %s", baseUrl));
            try {
                driver.get(baseUrl);
            } catch (Exception e) {
                close();
                throw new CommandException(String.format("Error fetching url %s", baseUrl));
            }
        }
    }

    /**
     * Executes the commands in the provided TestCase, running on a fresh driver instance each time.
     * Each command is executed with access to the test's context for variable resolution.
     *
     * @param test The TestCase containing the sequence of commands to execute.
     */
    public void runTest(TestCase test) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));

            test.clearContext();

            for (Command command : test.getCommands()) {
                command.execute(driver, wait, test.getContext());
            }

            close();
        } catch (CommandException e) {
            close();
            throw e;
        } catch (Exception e) {
            log.error(String.format("Test execution failed: %s", e.getMessage()));
            close();
            throw e;
        }
    }

    /**
     * Closes the WebDriver session, releasing any resources used during the test execution.
     */
    public void close() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                log.error(String.format("Error while closing WebDriver: %s", e.getMessage()));
            }
        }
    }
}