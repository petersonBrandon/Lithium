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
import com.lithium.commands.LogCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * The TestRunner class is responsible for managing the WebDriver session and executing a series of commands
 * associated with a test case. It initializes the WebDriver in either headless or maximized mode,
 * depending on the specified options.
 */
public class TestRunner {
    private static final Logger log = LogManager.getLogger(TestRunner.class);
    private static final int TIMEOUT_SECONDS = 10;

    private WebDriver driver;
    private WebDriverWait wait;
    private ChromeOptions options;

    /**
     * Constructs a TestRunner with specified options for headless and maximized browser settings.
     *
     * @param headless   If true, the WebDriver will run in headless mode (no UI display).
     * @param maximized  If true, the WebDriver will start in maximized window mode.
     */
    public TestRunner(boolean headless, boolean maximized) {
        this.options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless");
        }
        if (maximized) {
            options.addArguments("--start-maximized");
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
            this.driver = new ChromeDriver(options);
            this.wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
            log.info("Running test: " + test.getName());

            // Clear any existing variables in the context before starting
            test.clearContext();

            // Execute each command with access to the test context
            for (Command command : test.getCommands()) {
                command.execute(driver, wait, test.getContext());
            }

            driver.quit();
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    /**
     * Closes the WebDriver session, releasing any resources used during the test execution.
     */
    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}