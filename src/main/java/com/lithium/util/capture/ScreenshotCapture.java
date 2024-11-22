package com.lithium.util.capture;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import com.lithium.util.logger.LithiumLogger;
import com.lithium.cli.util.ProjectConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

public class ScreenshotCapture {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private static final int MAX_SCREENSHOTS = 50; // Default max screenshots

    /**
     * Captures a screenshot when a test fails.
     *
     * @param driver   The WebDriver instance
     * @param testName The name of the test that failed
     * @param config   The project configuration
     * @return The path to the saved screenshot file
     */
    public static String captureScreenshot(WebDriver driver, String testName, ProjectConfig config) {
        // Check if screenshots are enabled in configuration
        if (config == null || !config.isEnableScreenshotsOnFailure()) {
            return null;
        }

        if (!(driver instanceof TakesScreenshot)) {
            log.error("Driver does not support screenshot capture");
            return null;
        }

        try {
            // Create screenshots directory if it doesn't exist
            Path screenshotDir = Paths.get(System.getProperty("user.dir"), "screenshots");
            Files.createDirectories(screenshotDir);

            // Manage screenshot count
            manageScreenshotCount(screenshotDir);

            // Generate unique filename with timestamp
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("%s_%s.png", testName, timestamp);
            Path screenshotPath = screenshotDir.resolve(filename);

            // Capture and save screenshot
            TakesScreenshot screenshotDriver = (TakesScreenshot) driver;
            File screenshot = screenshotDriver.getScreenshotAs(OutputType.FILE);
            Files.copy(screenshot.toPath(), screenshotPath);

            log.info("Screenshot saved: " + screenshotPath);
            return screenshotPath.toString();
        } catch (IOException e) {
            log.error("Failed to capture screenshot: " + e.getMessage());
            return null;
        }
    }

    /**
     * Manages the number of screenshots to prevent disk space issues.
     * Removes oldest screenshots if count exceeds MAX_SCREENSHOTS.
     *
     * @param screenshotDir Directory containing screenshots
     * @throws IOException if there's an error managing screenshot files
     */
    private static void manageScreenshotCount(Path screenshotDir) throws IOException {
        try (Stream<Path> files = Files.list(screenshotDir)) {
            long screenshotCount = files.count();

            if (screenshotCount > MAX_SCREENSHOTS) {
                try (Stream<Path> oldFiles = Files.list(screenshotDir)) {
                    oldFiles
                            .sorted(Comparator.comparingLong(path -> {
                                try {
                                    return Files.getLastModifiedTime(path).toMillis();
                                } catch (IOException e) {
                                    return Long.MAX_VALUE;
                                }
                            }))
                            .limit(screenshotCount - MAX_SCREENSHOTS)
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                    log.info("Deleted old screenshot: " + path);
                                } catch (IOException e) {
                                    log.error("Failed to delete old screenshot: " + path);
                                }
                            });
                }
            }
        }
    }
}
