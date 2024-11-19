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
    private static final Logger log = LogManager.getLogger(TestRunner.class);

    private final WebDriver driver;
    private final int timeout;

    /**
     * Constructs a TestRunner with specified options for headless and maximized browser settings.
     *
     * @param headless   If true, the WebDriver will run in headless mode (no UI display).
     * @param maximized  If true, the WebDriver will start in maximized window mode.
     */
    public TestRunner(boolean headless, boolean maximized, String browser, int timeout) {
        this.timeout = timeout;
        BrowserDriverManager browserManager = new BrowserDriverManager();
        this.driver = browserManager.createDriver(browser, headless, maximized);

        // Set default timeouts
        this.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeout));
        this.driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeout));
        this.driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(timeout));
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
        } catch (Exception e) {
            log.error("Test execution failed: {}", e.getMessage());
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
                log.error("Error while closing WebDriver: {}", e.getMessage());
            }
        }
    }
}