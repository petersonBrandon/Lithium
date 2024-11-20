/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: TestParser.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.parser;

import com.lithium.core.TestCase;
import com.lithium.commands.Command;
import com.lithium.exceptions.TestSyntaxException;
import com.lithium.util.logger.LithiumLogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class TestParser {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private static final Pattern TEST_PATTERN = Pattern.compile("test\\s+\"([^\"]+)\"\\s*\\{\\s*$");

    private final Map<String, TestCase> testCases;
    private final CommandParser commandParser;

    /**
     * Constructs the TestParser object
     */
    public TestParser() {
        this.testCases = new HashMap<>();
        this.commandParser = new CommandParser();
    }

    /**
     * Parse the specified test file for execution.
     *
     * @param filePath path to test file location
     * @return A map of test cases
     * @throws IOException if file cannot be read
     * @throws TestSyntaxException if .lit file syntax is incorrect
     */
    public Map<String, TestCase> parseFile(String filePath) throws IOException, TestSyntaxException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            TestCase currentTest = null;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                Matcher testMatcher = TEST_PATTERN.matcher(line);
                if (testMatcher.matches()) {
                    String testName = testMatcher.group(1);
                    currentTest = new TestCase(testName);
                    testCases.put(testName, currentTest);
                } else if (line.equals("}")) {
                    currentTest = null;
                } else if (currentTest != null) {
                    Command command = commandParser.parseCommand(line, lineNumber);
                    if (command != null) {
                        currentTest.addCommand(command);
                    }
                }
            }

            if (currentTest != null) {
                throw new TestSyntaxException("Missing closing brace for test: " + currentTest.getName(), lineNumber);
            }
        }
        return testCases;
    }
}