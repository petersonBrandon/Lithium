/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: CommandRegistry.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.cli;

import com.lithium.cli.commands.InitCommand;
import com.lithium.cli.commands.RunCommand;
import com.lithium.cli.util.LithiumTerminal;
import org.jline.reader.*;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.util.HashMap;
import java.util.Map;

/**
 * Command registry to manage all available commands with JLine integration
 */
public class CommandRegistry {
    private static final Map<String, LithiumCommand> commands = new HashMap<>();
    private static final LithiumTerminal terminal = LithiumTerminal.getInstance();

    static {
        // Register all commands here
        registerCommand("run", new RunCommand());
        registerCommand("init", new InitCommand());
    }

    /**
     * Register a command to the registry
     *
     * @param name    name of the command to register
     * @param command command object to register
     */
    public static void registerCommand(String name, LithiumCommand command) {
        commands.put(name.toLowerCase(), command);
    }

    /**
     * Get a command by name from the registry
     *
     * @param name command name to retrieve
     * @return LithiumCommand object
     */
    public static LithiumCommand getCommand(String name) {
        return commands.get(name.toLowerCase());
    }

    /**
     * Display all command help options to the console with formatted output
     */
    public static void displayHelp() {
        terminal.printLogo();

        // Print header
        terminal.println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(LithiumTerminal.MAGENTA).bold())
                .append("Available Commands")
                .style(AttributedStyle.DEFAULT)
                .append("\n"));

        terminal.printSeparator(true);
        terminal.println("");

        // Print each command with enhanced formatting
        commands.forEach((name, command) -> {
            // Command name with cyan background
            terminal.println(new AttributedStringBuilder()
                    .style(AttributedStyle.DEFAULT.foreground(LithiumTerminal.CYAN).bold())
                    .append("  ")
                    .append(name.toUpperCase()));

            // Description with yellow accent
            terminal.println(new AttributedStringBuilder()
                    .style(AttributedStyle.DEFAULT.foreground(LithiumTerminal.YELLOW))
                    .append("    └─ ")
                    .style(AttributedStyle.DEFAULT)
                    .append(command.getDescription()));

            // Usage with green accent
            terminal.println(new AttributedStringBuilder()
                    .style(AttributedStyle.DEFAULT.foreground(LithiumTerminal.GREEN))
                    .append("       Usage: ")
                    .style(AttributedStyle.DEFAULT.faint())
                    .append(command.getUsage())
                    .append("\n"));
        });

        // Print footer
        terminal.printSeparator(true);
        terminal.println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.faint())
                .append("Use '<command> --help' for more information about a command.\n"));
    }

    /**
     * Read a line of input with command history and completion
     *
     * @param prompt The prompt to display
     * @return The input line
     */
    public static String readLine(String prompt) {
        try {
            return terminal.readLine(prompt);
        } catch (UserInterruptException e) {
            // Handle Ctrl+C
            return null;
        } catch (EndOfFileException e) {
            // Handle Ctrl+D
            return null;
        }
    }
}