package com.lithium.cli.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProjectConfig {
    private final String projectName;
    private String description = "";
    private String version = "1.0.0";
    private String author = "";
    private boolean cliOverride = true;
    private String baseUrl = "";
    private String browser = "chrome";
    private int defaultTimeout = 30;
    private String testDirectory = "tests";
    private boolean headless = false;
    private boolean maximizeWindow = true;
    private final ParallelExecutionConfig parallelExecution = new ParallelExecutionConfig();
    private String[] reportFormat = {"pdf", "html", "json"};
    private String reportDirectory = "reports";
    private boolean enableScreenshotsOnFailure = true;
    private String logLevel = "info";
    private String logDirectory = "logs";
    private boolean saveExecutionLogs = true;
    private final Map<String, EnvironmentConfig> environments = new HashMap<>();
    private String activeEnvironment = "";

    public ProjectConfig() {
        this.projectName = "Lithium Project";
        initializeDefaultEnvironments();
    }

    @JsonCreator
    public ProjectConfig(@JsonProperty("projectName") String projectName) {
        this.projectName = projectName;
        initializeDefaultEnvironments();
    }

    private void initializeDefaultEnvironments() {
        environments.put("dev", new EnvironmentConfig("", ""));
        environments.put("staging", new EnvironmentConfig("", ""));
        environments.put("production", new EnvironmentConfig("", ""));
    }

    // Getters
    public String getProjectName() { return projectName; }
    public String getDescription() { return description; }
    public String getVersion() { return version; }
    public String getAuthor() { return author; }
    public boolean getCliOverride() { return cliOverride; }
    public String getBaseUrl() { return baseUrl; }
    public String getBrowser() { return browser; }
    public int getDefaultTimeout() { return defaultTimeout; }
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
    public void setCliOverride(boolean cliOverride) { this.cliOverride = cliOverride; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl != null ? baseUrl.trim() : ""; }
    public void setBrowser(String browser) { this.browser = browser != null ? browser : "chrome"; }
    public void setDefaultTimeout(int defaultTimeout) { this.defaultTimeout = defaultTimeout; }
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

    public static class EnvironmentConfig {
        private String baseUrl;
        private String browser;

        public EnvironmentConfig() {
        }

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

        public ParallelExecutionConfig() {
        }

        public boolean isEnabled() { return enabled; }
        public int getThreadCount() { return threadCount; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public void setThreadCount(int threadCount) { this.threadCount = threadCount; }
    }
}
