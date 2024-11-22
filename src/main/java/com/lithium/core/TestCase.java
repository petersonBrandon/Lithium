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
import java.util.stream.Collectors;

public class TestCase {
    private final String name;
    private final String filePath;
    private final List<Stmt> statements;

    public TestCase(String name, String filePath, List<Stmt> statements) {
        this.name = name;
        this.filePath = filePath;
        this.statements = statements;
    }

    public String getName() {
        return name;
    }

    public String getFilePath() {
        return filePath;
    }

    public List<Stmt> getStatements() {
        return statements;
    }

    /**
     * Gets all global import statements for this test case
     */
    public List<Stmt.Import> getImports() {
        return statements.stream()
                .filter(stmt -> stmt instanceof Stmt.Import)
                .map(stmt -> (Stmt.Import) stmt)
                .collect(Collectors.toList());
    }

    /**
     * Gets all global function declarations for this test case
     */
    public List<Stmt.Function> getFunctions() {
        return statements.stream()
                .filter(stmt -> stmt instanceof Stmt.Function)
                .map(stmt -> (Stmt.Function) stmt)
                .collect(Collectors.toList());
    }

    /**
     * Gets all global variable declarations for this test case
     */
    public List<Stmt.Var> getGlobalVariables() {
        return statements.stream()
                .filter(stmt -> stmt instanceof Stmt.Var)
                .map(stmt -> (Stmt.Var) stmt)
                .collect(Collectors.toList());
    }

    /**
     * Gets the actual test statements (excluding global declarations)
     */
    public List<Stmt> getTestStatements() {
        return statements.stream()
                .filter(stmt -> !(stmt instanceof Stmt.Import ||
                        stmt instanceof Stmt.Function ||
                        stmt instanceof Stmt.Var))
                .collect(Collectors.toList());
    }
}