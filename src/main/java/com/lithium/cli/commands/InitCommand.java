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
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
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

    private Terminal terminal;
    private LineReader lineReader;
    private boolean quickMode;

    private static final String BANNER = """
    ╦  ┬┌┬┐┬ ┬┬┬ ┬┌┬┐
    ║  │ │ ├─┤││ ││││
    ╩═╝┴ ┴ ┴ ┴┴└─┘┴ ┴
    Test Automation Framework
    """;

    private static final String[] SPINNER_FRAMES = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
    private static final String CHECK_MARK = "✓";
    private static final String CROSS_MARK = "✗";
    private static final String ARROW = "→";

    public InitCommand() {
        try {
            this.terminal = TerminalBuilder.builder()
                    .system(true)
                    .dumb(true)      // Allow dumb terminal as fallback
                    .jansi(true)     // Enable Jansi support
                    .build();

            this.lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .parser(new DefaultParser())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize terminal: " + e.getMessage(), e);
        }
    }

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

        printBanner();
        printWelcome();

        // Step 1: Project Name
        printStep(1, "Project Setup");
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

            // Step 2: Creating Project Structure
            printStep(2, "Creating Project Structure");
            showSpinner("Creating directories", 1000);
            createProjectStructure(projectName, config);

            // Step 3: Generating Configuration
            printStep(3, "Generating Configuration");
            showSpinner("Writing configuration file", 800);
            generateConfigFile(config);

            // Step 4: Sample Test (if applicable)
            if (!quickMode && promptForSampleTest()) {
                printStep(4, "Creating Sample Test");
                showSpinner("Generating sample test file", 600);
                generateSampleTest(projectName, config);
            }

            printSuccess(projectName);
        } catch (Exception e) {
            printError("Failed to initialize project: " + e.getMessage());
            System.exit(1);
        }
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
        String response = lineReader.readLine("Would you like to create a sample test file? (yes/no, default is yes): ");
        return response.isEmpty() || response.toLowerCase().startsWith("y");
    }

    private void generateSampleTest(String projectName, ProjectConfig config) throws IOException {
        Path testPath = Paths.get(projectName, config.getTestDirectory(), "sampleTest.lit");
        Files.write(testPath, SAMPLE_TEST_CONTENT.getBytes());
    }

    private void printWelcome() {
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN))
                .append("Welcome to Lithium! Let's set up your new test automation project.")
                .toAnsi());
        terminal.flush();
    }

    private void printInfo(String message) {
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT)
                .append(message)
                .toAnsi());
        terminal.flush();
    }

    private String promptForProjectName() {
        while (true) {
            String name = lineReader.readLine("Enter a project name (no spaces allowed): ");
            if (validateProjectName(name)) {
                return name;
            }
        }
    }

    private boolean validateProjectName(String name) {
        if (!VALID_PROJECT_NAME.matcher(name).matches()) {
            printError("Error: Project names cannot contain spaces or special characters. Please try again.");
            return false;
        }

        File projectDir = new File(name);
        if (projectDir.exists()) {
            printError("Error: A folder named '" + name + "' already exists in this location. Please choose a different name.");
            return false;
        }

        return true;
    }

    private void createProjectStructure(String projectName, ProjectConfig config) throws IOException {
        // Create main project directory
        Path projectPath = Paths.get(projectName);
        Files.createDirectory(projectPath);

        // Create subdirectories
        String[] directories = {config.getTestDirectory(), "reports"};
        for (String dir : directories) {
            Files.createDirectory(projectPath.resolve(dir));
        }
    }

    private void printError(String message) {
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                .append(message)
                .toAnsi());
        terminal.flush();
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
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
                .append("\nLithium Project '" + projectName + "' successfully initialized!")
                .toAnsi());

        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT)
                .append("\nYou can now add your test files to the 'tests' folder and run them with 'lit run'.")
                .toAnsi());

        terminal.flush();
    }

    private void showSpinner(String message, int durationMs) {
        Thread spinnerThread = new Thread(() -> {
            int frame = 0;
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < durationMs) {
                terminal.writer().print("\r" + SPINNER_FRAMES[frame] + " " + message);
                terminal.flush();
                frame = (frame + 1) % SPINNER_FRAMES.length;
                try {
                    Thread.sleep(80);
                } catch (InterruptedException e) {
                    break;
                }
            }
            terminal.writer().print("\r" + CHECK_MARK + " " + message + "\n");
            terminal.flush();
        });
        spinnerThread.start();
        try {
            spinnerThread.join();
        } catch (InterruptedException e) {
            // Handle interruption
        }
    }

    private void printBanner() {
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN).bold())
                .append(BANNER)
                .toAnsi());
        terminal.flush();
    }

    private void printStep(int stepNumber, String message) {
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
                .append("\nStep " + stepNumber + ": ")
                .style(AttributedStyle.DEFAULT)
                .append(message)
                .toAnsi());
        terminal.flush();
    }

    private void printInputPrompt(String prompt) {
        terminal.writer().print(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
                .append(ARROW + " ")
                .style(AttributedStyle.DEFAULT)
                .append(prompt)
                .toAnsi());
        terminal.flush();
    }

    // Update promptForConfiguration
    private ProjectConfig promptForConfiguration(String projectName) {
        ProjectConfig config = new ProjectConfig(projectName);

        // Base URL
        printStep(2, "Configuration Setup");
        printInputPrompt("Enter a base URL for your tests (leave blank to configure later): ");
        String baseUrl = lineReader.readLine();
        config.setBaseUrl(baseUrl);

        // Browser Selection
        printInfo("\nAvailable Browsers:");
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN))
                .append("  1. Chrome  [default]\n")
                .append("  2. Firefox\n")
                .append("  3. Edge\n")
                .append("  4. Safari")
                .toAnsi());
        terminal.flush();

        printInputPrompt("Select browser (1-4): ");
        String browserChoice = lineReader.readLine();
        config.setBrowser(getBrowserFromChoice(browserChoice));

        // Timeout
        printInputPrompt("Enter the default timeout in seconds (default: 30): ");
        String timeoutStr = lineReader.readLine();
        config.setDefaultTimeout(parseTimeout(timeoutStr));

        // Tests Folder
        printInputPrompt("Enter the root test folder name (default: tests): ");
        String testsFolder = lineReader.readLine();
        config.setTestDirectory(testsFolder);

        return config;
    }
}