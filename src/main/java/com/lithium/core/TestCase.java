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
 * It holds the name of the test, a list of commands to be executed during the test,
 * and manages the test context for variable storage and resolution.
 */
public class TestCase {
    private final String name;
    private final List<Command> commands;
    private final TestContext context;

    /**
     * Constructs a TestCase with the specified name.
     *
     * @param name The name of the test case.
     */
    public TestCase(String name) {
        this.name = name;
        this.commands = new ArrayList<>();
        this.context = new TestContext();
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

    /**
     * Gets the test context associated with this test case.
     *
     * @return The TestContext instance for this test case.
     */
    public TestContext getContext() {
        return context;
    }

    /**
     * Clears all variables in the test context.
     * This can be useful when needing to reset the test state.
     */
    public void clearContext() {
        context.clear();
    }
}