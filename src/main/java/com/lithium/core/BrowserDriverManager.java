package com.lithium.core;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

public class BrowserDriverManager {
    private WebDriver driver;

    public WebDriver createDriver(String browserName, boolean headless, boolean maximized) {
        browserName = browserName.toLowerCase();

        switch (browserName) {
            case "chrome":
                setupChromeDriver(headless, maximized);
                break;
            case "firefox":
                setupFirefoxDriver(headless, maximized);
                break;
            case "edge":
                setupEdgeDriver(headless, maximized);
                break;
            case "safari":
                setupSafariDriver(maximized);
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browserName);
        }

        return driver;
    }

    private void setupChromeDriver(boolean headless, boolean maximized) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        if (headless) {
            options.addArguments("--headless=new");
        }
        if (maximized) {
            options.addArguments("--start-maximized");
        }

        // Add common Chrome options for better stability
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
    }

    private void setupFirefoxDriver(boolean headless, boolean maximized) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();

        if (headless) {
            options.addArguments("-headless");
        }

        driver = new FirefoxDriver(options);

        if (maximized) {
            driver.manage().window().maximize();
        }
    }

    private void setupEdgeDriver(boolean headless, boolean maximized) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();

        if (headless) {
            options.addArguments("--headless=new");
        }
        if (maximized) {
            options.addArguments("--start-maximized");
        }

        driver = new EdgeDriver(options);
    }

    private void setupSafariDriver(boolean maximized) {
        // Safari doesn't need WebDriverManager setup as it's built into macOS
        SafariOptions options = new SafariOptions();
        driver = new SafariDriver(options);

        if (maximized) {
            driver.manage().window().maximize();
        }
    }

    public void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
