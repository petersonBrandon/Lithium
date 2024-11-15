/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: TestContext.java
 * Author: Brandon Peterson
 * Date: 11/14/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The TestContext class manages the execution context for test cases, including variable storage
 * and variable resolution. It provides methods to store and retrieve variables, as well as
 * resolve variable references in strings.
 */
public class TestContext {
    private static final Logger log = LogManager.getLogger(TestContext.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private final Map<String, String> variables;

    /**
     * Constructs a new TestContext with an empty variable map.
     */
    public TestContext() {
        this.variables = new HashMap<>();
    }

    /**
     * Sets a variable value in the context.
     *
     * @param name The name of the variable
     * @param value The value to store
     */
    public void setVariable(String name, String value) {
        variables.put(name, value);
    }

    /**
     * Gets a variable value from the context.
     *
     * @param name The name of the variable
     * @return The value of the variable, or null if not found
     */
    public String getVariable(String name) {
        return variables.get(name);
    }

    /**
     * Checks if a variable exists in the context.
     *
     * @param name The name of the variable
     * @return true if the variable exists, false otherwise
     */
    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    /**
     * Resolves any variable references in the given string.
     * Variables are referenced using ${variableName} syntax.
     *
     * @param input The input string containing potential variable references
     * @return The string with all variable references resolved
     * @throws IllegalArgumentException if a referenced variable doesn't exist
     */
    public String resolveVariables(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String varName = matcher.group(1);
            if (!hasVariable(varName)) {
                throw new IllegalArgumentException("Undefined variable referenced: " + varName);
            }
            String value = getVariable(varName);
            // Escape any special regex characters in the replacement value
            matcher.appendReplacement(result, value.replace("$", "\\$"));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Clears all variables from the context.
     */
    public void clear() {
        variables.clear();
    }

    /**
     * Gets a read-only view of all variables in the context.
     *
     * @return Map of all variables and their values
     */
    public Map<String, String> getAllVariables() {
        return Map.copyOf(variables);
    }
}