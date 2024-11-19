/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: InitCommand.java
 * Author: Brandon Peterson
 * Date: 11/17/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.cli.commands;

import com.lithium.cli.BaseLithiumCommand;
import com.lithium.cli.util.ProjectConfig;
import com.lithium.cli.util.LithiumTerminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Pattern;

public class InitCommand extends BaseLithiumCommand {
    private static final Pattern VALID_PROJECT_NAME = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private static final String SAMPLE_TEST_CONTENT =
            "test \"Sample Test\" {\n" +
                    "    open \"https://example.com\"\n" +
                    "    click link \"About Us\"\n" +
                    "    wait tag \"h1\" visible 10\n" +
                    "    assertText tag \"h1\" \"About Us\"\n" +
                    "}";

    private final LithiumTerminal terminal = LithiumTerminal.getInstance();
    private boolean quickMode;

    @Override
    public String getDescription() {
        return "Initialize a new Lithium test automation project";
    }

    @Override
    public String getUsage() {
        return "lit init [project-name] [--quick]";
    }

    @Override
    public void execute(String[] args) {
        quickMode = Arrays.asList(args).contains("--quick");
        String projectName;

        terminal.printLogo();
        printWelcome();

        // Step 1: Project Name
        terminal.printStep(1, "Project Setup");
        if (args.length > 1 && !args[1].startsWith("--")) {
            projectName = args[1];
            if (!validateProjectName(projectName)) {
                System.exit(1);
            }
        } else {
            projectName = promptForProjectName();
        }

        try {
            ProjectConfig config = quickMode ?
                    new ProjectConfig(projectName) :
                    promptForConfiguration(projectName);

            terminal.printSeparator(true);

            // Step 2: Creating Project Structure
            terminal.printStep(2, "Creating Project Structure");
            terminal.showSpinner("Creating directories", 1000);
            createProjectStructure(projectName, config);

            // Step 3: Generating Configuration
            terminal.printStep(3, "Generating Configuration");
            terminal.showSpinner("Writing configuration file", 800);
            generateConfigFile(config);

            // Step 4: Sample Test (if applicable)
            if (!quickMode && promptForSampleTest()) {
                terminal.printStep(4, "Creating Sample Test");
                terminal.showSpinner("Generating sample test file", 600);
                generateSampleTest(projectName, config);
            }

            terminal.printSeparator(true);
            printSuccess(projectName);
        } catch (Exception e) {
            terminal.printError("Failed to initialize project: " + e.getMessage());
            System.exit(1);
        }
    }

    private void printWelcome() {
        terminal.println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(LithiumTerminal.CYAN))
                .append("Welcome to Lithium! Let's set up your new test automation project.\n"));
        terminal.printSeparator(true);
    }

    private String getBrowserFromChoice(String choice) {
        return switch (choice.trim()) {
            case "2" -> "firefox";
            case "3" -> "edge";
            case "4" -> "safari";
            default -> "chrome";
        };
    }

    private int parseTimeout(String timeoutStr) {
        if (timeoutStr == null || timeoutStr.trim().isEmpty()) {
            return 30;
        }
        try {
            int timeout = Integer.parseInt(timeoutStr.trim());
            return timeout > 0 ? timeout : 30;
        } catch (NumberFormatException e) {
            return 30;
        }
    }

    private boolean promptForSampleTest() {
        String response = terminal.readLine("Would you like to create a sample test file? (yes/no, default is yes): ");
        return response.isEmpty() || response.toLowerCase().startsWith("y");
    }

    private void generateSampleTest(String projectName, ProjectConfig config) throws IOException {
        Path testPath = Paths.get(projectName, config.getTestDirectory(), "sampleTest.lit");
        Files.write(testPath, SAMPLE_TEST_CONTENT.getBytes());
    }

    private String promptForProjectName() {
        while (true) {
            String name = terminal.readLine("Enter a project name (no spaces allowed): ");
            if (validateProjectName(name)) {
                return name;
            }
        }
    }

    private boolean validateProjectName(String name) {
        if (!VALID_PROJECT_NAME.matcher(name).matches()) {
            terminal.printError("Error: Project names cannot contain spaces or special characters. Please try again.");
            return false;
        }

        File projectDir = new File(name);
        if (projectDir.exists()) {
            terminal.printError("Error: A folder named '" + name + "' already exists in this location. Please choose a different name.");
            return false;
        }

        return true;
    }

    private void createProjectStructure(String projectName, ProjectConfig config) throws IOException {
        Path projectPath = Paths.get(projectName);
        Files.createDirectory(projectPath);

        String[] directories = {config.getTestDirectory(), "reports"};
        for (String dir : directories) {
            Files.createDirectory(projectPath.resolve(dir));
        }
    }

    private void generateConfigFile(ProjectConfig config) throws IOException {
        Path configPath = Paths.get(config.getProjectName(), "lithium.config.json");

        try (FileWriter writer = new FileWriter(configPath.toFile())) {
            StringBuilder json = new StringBuilder();
            json.append("{\n");

            // Project metadata
            appendJsonProperty(json, "projectName", config.getProjectName(), true);
            appendJsonProperty(json, "description", config.getDescription(), true);
            appendJsonProperty(json, "version", config.getVersion(), true);
            appendJsonProperty(json, "author", config.getAuthor(), true);

            // Core settings
            appendJsonProperty(json, "cliOverride", config.getCliOverride(), true);
            appendJsonProperty(json, "baseUrl", config.getBaseUrl(), true);
            appendJsonProperty(json, "defaultTimeout", String.valueOf(config.getDefaultTimeout()), true);
            appendJsonProperty(json, "browser", config.getBrowser(), true);
            appendJsonProperty(json, "headless", config.isHeadless(), true);
            appendJsonProperty(json, "maximizeWindow", config.isMaximizeWindow(), true);

            // Parallel execution
            json.append("    \"parallelExecution\": {\n");
            json.append("        \"enabled\": ").append(config.getParallelExecution().isEnabled()).append(",\n");
            json.append("        \"threadCount\": ").append(config.getParallelExecution().getThreadCount()).append("\n");
            json.append("    },\n");

            // Directory and file settings
            appendJsonProperty(json, "testDirectory", config.getTestDirectory(), true);
            appendJsonProperty(json, "reportDirectory", config.getReportDirectory(), true);
            appendJsonProperty(json, "logDirectory", config.getLogDirectory(), true);

            // Report format array
            json.append("    \"reportFormat\": [\n");
            String[] formats = config.getReportFormat();
            for (int i = 0; i < formats.length; i++) {
                json.append("        \"").append(formats[i]).append("\"");
                if (i < formats.length - 1) json.append(",");
                json.append("\n");
            }
            json.append("    ],\n");

            // Reporting and logging settings
            appendJsonProperty(json, "enableScreenshotsOnFailure", config.isEnableScreenshotsOnFailure(), true);
            appendJsonProperty(json, "logLevel", config.getLogLevel(), true);
            appendJsonProperty(json, "saveExecutionLogs", config.isSaveExecutionLogs(), true);

            // Environments
            json.append("    \"environments\": {\n");
            String[] envOrder = {"dev", "staging", "production"};
            for (int i = 0; i < envOrder.length; i++) {
                String envName = envOrder[i];
                ProjectConfig.EnvironmentConfig envConfig = config.getEnvironments().get(envName);
                if (envConfig != null) {
                    json.append("        \"").append(envName).append("\": {\n");
                    json.append("            \"baseUrl\": \"").append(envConfig.getBaseUrl()).append("\",\n");
                    json.append("            \"browser\": \"").append(envConfig.getBrowser()).append("\"\n");
                    json.append("        }");
                    if (i < envOrder.length - 1) json.append(",");
                    json.append("\n");
                }
            }
            json.append("    },\n");

            // Active environment (last property, no comma)
            appendJsonProperty(json, "activeEnvironment", config.getActiveEnvironment(), false);

            json.append("}");
            writer.write(json.toString());
        }
    }

    private void appendJsonProperty(StringBuilder json, String key, String value, boolean addComma) {
        json.append("    \"").append(key).append("\": \"").append(value).append("\"");
        if (addComma) json.append(",");
        json.append("\n");
    }

    private void appendJsonProperty(StringBuilder json, String key, boolean value, boolean addComma) {
        json.append("    \"").append(key).append("\": ").append(value);
        if (addComma) json.append(",");
        json.append("\n");
    }

    private void printSuccess(String projectName) {
        terminal.println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(LithiumTerminal.GREEN).bold())
                .append("\n✓ Project Successfully Initialized!\n"));

        terminal.println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(LithiumTerminal.CYAN))
                .append("Project: ")
                .style(AttributedStyle.DEFAULT)
                .append(projectName)
                .append("\n"));

        terminal.println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT)
                .append("Next steps:\n")
                .append("  1. ")
                .style(AttributedStyle.DEFAULT.foreground(LithiumTerminal.YELLOW))
                .append("cd " + projectName)
                .style(AttributedStyle.DEFAULT)
                .append("\n  2. Add your test files to the 'tests' folder\n")
                .append("  3. ")
                .style(AttributedStyle.DEFAULT.foreground(LithiumTerminal.YELLOW))
                .append("lit run <test-name>")
                .style(AttributedStyle.DEFAULT)
                .append(" to execute your tests\n"));
    }

    private ProjectConfig promptForConfiguration(String projectName) {
        ProjectConfig config = new ProjectConfig(projectName);

        // Base URL
        terminal.printStep(2, "Configuration Setup");
        String baseUrl = terminal.readLine("Enter a base URL for your tests (leave blank to configure later): ");
        config.setBaseUrl(baseUrl);

        // Browser Selection
        terminal.printInfo("\nAvailable Browsers:");
        terminal.println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(LithiumTerminal.CYAN))
                .append("  1. Chrome  [default]\n")
                .append("  2. Firefox\n")
                .append("  3. Edge\n")
                .append("  4. Safari"));

        String browserChoice = terminal.readLine("Select browser (1-4): ");
        config.setBrowser(getBrowserFromChoice(browserChoice));

        // Timeout
        String timeoutStr = terminal.readLine("Enter the default timeout in seconds (default: 30): ");
        config.setDefaultTimeout(parseTimeout(timeoutStr));

        // Tests Folder
        String testsFolder = terminal.readLine("Enter the root test folder name (default: tests): ");
        config.setTestDirectory(testsFolder);

        return config;
    }
}