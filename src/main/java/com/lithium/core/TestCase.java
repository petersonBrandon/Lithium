/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: TestCase.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.core;

import com.lithium.commands.Command;
import java.util.ArrayList;
import java.util.List;

/**
 * The TestCase class represents a single test case in the Lithium framework.
 * It holds the name of the test and a list of commands to be executed during the test.
 */
public class TestCase {
    private final String name;
    private final List<Command> commands;

    /**
     * Constructs a TestCase with the specified name.
     *
     * @param name The name of the test case.
     */
    public TestCase(String name) {
        this.name = name;
        this.commands = new ArrayList<>();
    }

    /**
     * Adds a command to the list of commands for this test case.
     *
     * @param command The Command to be added to the test case.
     */
    public void addCommand(Command command) {
        commands.add(command);
    }

    /**
     * Gets the list of commands associated with this test case.
     *
     * @return A list of Command objects to be executed during the test case.
     */
    public List<Command> getCommands() {
        return commands;
    }

    /**
     * Gets the name of the test case.
     *
     * @return The name of the test case.
     */
    public String getName() {
        return name;
    }
}
