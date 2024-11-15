/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: CommandRegistry.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.cli;

import com.lithium.cli.commands.RunCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * Command registry to manage all available commands
 */
public class CommandRegistry {
    private static final Map<String, LithiumCommand> commands = new HashMap<>();

    static {
        // Register all commands here
        registerCommand("run", new RunCommand());
        // Add more commands as needed
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
     * Display all command help options to the console
     */
    public static void displayHelp() {
        System.out.println("Available commands:");
        commands.forEach((name, command) -> {
            System.out.printf("  %-15s %s%n", name, command.getDescription());
            System.out.printf("    Usage: %s%n", command.getUsage());
        });
    }
}
