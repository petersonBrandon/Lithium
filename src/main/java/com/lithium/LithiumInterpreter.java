/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: LithiumInterpreter.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium;

import com.lithium.commands.ClickCommand;
import com.lithium.core.TestCase;
import com.lithium.core.TestRunner;
import com.lithium.exceptions.TestSyntaxException;
import com.lithium.parser.TestParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Map;

/**
 * The LithiumInterpreter class is responsible for executing Lithium test files (.lit).
 * It parses the specified file and runs test cases either in headless or maximized mode,
 * depending on the arguments provided.
 */
public class LithiumInterpreter {
    private static final Logger log = LogManager.getLogger(LithiumInterpreter.class);


    /**
     * Main method that serves as the entry point for running Lithium test files.
     *
     * @param args Command-line arguments specifying the operation and options for running tests.
     *             Usage: lit run <file-name> [test-name] [--headed] [--maximized]
     *             <ul>
     *               <li><code>run</code>: Command to execute a test file.</li>
     *               <li><code>file-name</code>: Name of the test file without the ".lit" extension.</li>
     *               <li><code>test-name</code> (optional): Specific test to run within the file.</li>
     *               <li><code>--headed</code> (optional): Run tests in headed mode.</li>
     *               <li><code>--maximized</code> (optional): Run tests in maximized window mode.</li>
     *             </ul>
     */
    public static void main(String[] args) {
        if (args.length < 2 || !args[0].equals("run")) {
            System.out.println("Usage: lit run <file-name> [test-name] [--headed] [--maximized]");
            return;
        }

        boolean headless = !Arrays.asList(args).contains("--headed");
        boolean maximized = Arrays.asList(args).contains("--maximized");
        TestRunner runner = null;

        try {
            String fileName = args[1];
            String testFilePath = System.getProperty("user.dir") + "\\" + fileName + ".lit";

            File testFile = new File(testFilePath);
            if (!testFile.exists()) {
                throw new FileNotFoundException("Test file '" + testFilePath + "' not found!");
            }

            TestParser parser = new TestParser();
            Map<String, TestCase> testCases = parser.parseFile(testFilePath);
            runner = new TestRunner(headless, maximized);  // Modified constructor

            if (args.length > 2 && !args[2].startsWith("--")) {
                String testName = args[2];
                TestCase test = testCases.get(testName);
                if (test == null) {
                    throw new IllegalArgumentException("Test '" + testName + "' not found!");
                }
                runner.runTest(test);
            } else {
                for (TestCase test : testCases.values()) {
                    runner.runTest(test);
                }
            }
        } catch (TestSyntaxException e) {
            log.fatal("Syntax error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            log.fatal("Error: " + e.getMessage());
            System.exit(1);
        } finally {
            if (runner != null) {
                runner.close();
            }
        }
    }
}