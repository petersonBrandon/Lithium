/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: LithiumInterpreter.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium;

import com.lithium.cli.CommandRegistry;
import com.lithium.cli.LithiumCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The LithiumInterpreter class is responsible for executing Lithium test files (.lit).
 * It parses the specified file and runs test cases either in headless or maximized mode,
 * depending on the arguments provided.
 */
public class LithiumInterpreter {
    private static final Logger log = LogManager.getLogger(LithiumInterpreter.class);

    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("--help") || args[0].equals("-h")) {
            CommandRegistry.displayHelp();
            return;
        }

        if (args[0].equals("--version") || args[0].equals("-v")) {
            CommandRegistry.displayVersion();
            return;
        }

        try {
            String commandName = args[0].toLowerCase();
            LithiumCommand command = CommandRegistry.getCommand(commandName);

            if (command == null) {
                System.out.println("Unknown command: " + commandName);
                CommandRegistry.displayHelp();
                return;
            }

            command.execute(args);

        } catch (Exception e) {
            log.fatal("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}