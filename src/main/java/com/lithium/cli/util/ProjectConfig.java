package com.lithium.cli.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProjectConfig {
    private final String projectName;
    private String description = "";
    private String version = "1.0.0";
    private String author = "";
    private String baseUrl = "";
    private String browser = "chrome";
    private int timeout = 30;
    private String testDirectory = "tests";
    private boolean headless = false;
    private boolean maximizeWindow = true;
    private ParallelExecutionConfig parallelExecution = new ParallelExecutionConfig();
    private String[] reportFormat = {"html", "json"};
    private String reportDirectory = "reports";
    private boolean enableScreenshotsOnFailure = true;
    private String logLevel = "info";
    private String logDirectory = "logs";
    private boolean saveExecutionLogs = true;
    private Map<String, EnvironmentConfig> environments = new HashMap<>();
    private String activeEnvironment = "dev";

    public ProjectConfig(String projectName) {
        this.projectName = projectName;
        initializeDefaultEnvironments();
    }

    private void initializeDefaultEnvironments() {
        environments.put("dev", new EnvironmentConfig("https://dev.example.com", "chrome"));
        environments.put("staging", new EnvironmentConfig("https://staging.example.com", "firefox"));
        environments.put("production", new EnvironmentConfig("https://example.com", "chrome"));
    }

    // Getters
    public String getProjectName() { return projectName; }
    public String getDescription() { return description; }
    public String getVersion() { return version; }
    public String getAuthor() { return author; }
    public String getBaseUrl() { return baseUrl; }
    public String getBrowser() { return browser; }
    public int getTimeout() { return timeout; }
    public String getTestDirectory() { return testDirectory; }
    public boolean isHeadless() { return headless; }
    public boolean isMaximizeWindow() { return maximizeWindow; }
    public ParallelExecutionConfig getParallelExecution() { return parallelExecution; }
    public String[] getReportFormat() { return reportFormat; }
    public String getReportDirectory() { return reportDirectory; }
    public boolean isEnableScreenshotsOnFailure() { return enableScreenshotsOnFailure; }
    public String getLogLevel() { return logLevel; }
    public String getLogDirectory() { return logDirectory; }
    public boolean isSaveExecutionLogs() { return saveExecutionLogs; }
    public Map<String, EnvironmentConfig> getEnvironments() { return environments; }
    public String getActiveEnvironment() { return activeEnvironment; }

    // Setters
    public void setDescription(String description) { this.description = description; }
    public void setVersion(String version) { this.version = version; }
    public void setAuthor(String author) { this.author = author; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl != null ? baseUrl.trim() : ""; }
    public void setBrowser(String browser) { this.browser = browser != null ? browser : "chrome"; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
    public void setTestDirectory(String testDirectory) {
        this.testDirectory = !Objects.equals(testDirectory, "") ? testDirectory : "tests";
    }
    public void setHeadless(boolean headless) { this.headless = headless; }
    public void setMaximizeWindow(boolean maximizeWindow) { this.maximizeWindow = maximizeWindow; }
    public void setReportFormat(String[] reportFormat) { this.reportFormat = reportFormat; }
    public void setReportDirectory(String reportDirectory) { this.reportDirectory = reportDirectory; }
    public void setEnableScreenshotsOnFailure(boolean enableScreenshotsOnFailure) {
        this.enableScreenshotsOnFailure = enableScreenshotsOnFailure;
    }
    public void setLogLevel(String logLevel) { this.logLevel = logLevel; }
    public void setLogDirectory(String logDirectory) { this.logDirectory = logDirectory; }
    public void setSaveExecutionLogs(boolean saveExecutionLogs) {
        this.saveExecutionLogs = saveExecutionLogs;
    }
    public void setActiveEnvironment(String activeEnvironment) {
        this.activeEnvironment = activeEnvironment;
    }

    public class EnvironmentConfig {
        private String baseUrl;
        private String browser;

        public EnvironmentConfig(String baseUrl, String browser) {
            this.baseUrl = baseUrl;
            this.browser = browser;
        }

        public String getBaseUrl() { return baseUrl; }
        public String getBrowser() { return browser; }
    }

    public static class ParallelExecutionConfig {
        private boolean enabled = true;
        private int threadCount = 4;

        public boolean isEnabled() { return enabled; }
        public int getThreadCount() { return threadCount; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public void setThreadCount(int threadCount) { this.threadCount = threadCount; }
    }
}