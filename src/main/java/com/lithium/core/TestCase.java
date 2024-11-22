/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: TestCase.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.core;

import com.lithium.parser.Stmt;

import java.util.List;

/**
 * The TestCase class represents a single test case in the Lithium framework.
 * It holds the name of the test, a list of commands to be executed during the test,
 * and manages the test context for variable storage and resolution.
 */
public class TestCase {
    private final String name;
    private final String filePath;
    private final List<Stmt.Command> commands;

    public TestCase(String name, String filePath, List<Stmt.Command> commands) {
        this.name = name;
        this.filePath = filePath;
        this.commands = commands;
    }

    public String getName() {
        return name;
    }

    public String getFilePath() {
        return filePath;
    }

    public List<Stmt.Command> getCommands() {
        return commands;
    }
}