package com.lithium.parser;

import com.lithium.lexer.Token;
import com.lithium.locators.LocatorType;

import java.util.List;
import java.util.Map;

public abstract class Stmt extends Node {
    Stmt(int line, int column) {
        super(line, column);
    }

    // Variable declarations
    static class Var extends Stmt {
        final Token name;
        final Expr initializer;

        Var(Token name, Expr initializer) {
            super(name.getLine(), name.getColumn());
            this.name = name;
            this.initializer = initializer;
        }
    }

    // Function declarations
    public static class Function extends Stmt {
        public final Token name;
        final List<Token> params;
        public final List<Stmt> body;

        Function(Token name, List<Token> params, List<Stmt> body) {
            super(name.getLine(), name.getColumn());
            this.name = name;
            this.params = params;
            this.body = body;
        }
    }

    // If statements
    static class If extends Stmt {
        final Expr condition;
        final List<Stmt> thenBranch;
        final List<Stmt> elseBranch;

        If(Expr condition, List<Stmt> thenBranch, List<Stmt> elseBranch) {
            super(condition.line, condition.column);
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
    }

    // While loops
    static class While extends Stmt {
        final Expr condition;
        final List<Stmt> body;

        While(Expr condition, List<Stmt> body) {
            super(condition.line, condition.column);
            this.condition = condition;
            this.body = body;
        }
    }

    // For loops
    static class For extends Stmt {
        final Token variable;
        final Expr range;
        final List<Stmt> body;

        For(Token variable, Expr range, List<Stmt> body) {
            super(variable.getLine(), variable.getColumn());
            this.variable = variable;
            this.range = range;
            this.body = body;
        }
    }

    // Import statement
    static class Import extends Stmt {
        final Token path;
        final Token alias;

        Import(Token path, Token alias) {
            super(path.getLine(), path.getColumn());
            this.path = path;
            this.alias = alias;
        }
    }

    // Export statement
    static class Export extends Stmt {
        final Token name;
        final boolean exportAll;

        Export(Token name, boolean exportAll) {
            super(name != null ? name.getLine() : 0,
                    name != null ? name.getColumn() : 0);
            this.name = name;
            this.exportAll = exportAll;
        }
    }

    // Enhanced Command statement with specific command types
    public static class Command extends Stmt {
        public enum CommandType {
            // UI Interactions
            CLICK, DOUBLE_CLICK, RIGHT_CLICK, HOVER, TYPE, CLEAR, SELECT,
            // Assertions
            ASSERT_TEXT, ASSERT_VISIBLE, ASSERT_URL,
            // Navigation
            OPEN, BACK, FORWARD, REFRESH, SWITCH_WINDOW, OPEN_TAB, CLOSE_TAB, SWITCH_TO_WINDOW,
            // Utility
            LOG, WAIT
        }

        public final CommandType commandType;
        final Token command;
        public final List<Expr> arguments;
        public final Map<String, Expr> namedArgs;
        public final LocatorType locatorType;

        Command(CommandType commandType, Token command, List<Expr> arguments,
                Map<String, Expr> namedArgs, LocatorType locatorType) {
            super(command.getLine(), command.getColumn());
            this.commandType = commandType;
            this.command = command;
            this.arguments = arguments;
            this.namedArgs = namedArgs;
            this.locatorType = locatorType;
        }
    }

    // Block statements (groups of statements in curly braces)
    static class Block extends Stmt {
        final List<Stmt> statements;

        Block(List<Stmt> statements) {
            super(statements.isEmpty() ? 0 : statements.get(0).line,
                    statements.isEmpty() ? 0 : statements.get(0).column);
            this.statements = statements;
        }
    }

    // Expression statements (expressions used as statements)
    static class Expression extends Stmt {
        final Expr expression;

        Expression(Expr expression) {
            super(expression.line, expression.column);
            this.expression = expression;
        }
    }

    static class Return extends Stmt {
        final Token keyword;
        final Expr value;

        Return(Token keyword, Expr value) {
            super(keyword.getLine(), keyword.getColumn());
            this.keyword = keyword;
            this.value = value;
        }
    }

    public static class Test extends Stmt {
        public final Token description;
        public final List<Stmt> body;

        Test(Token description, List<Stmt> body) {
            super(description.getLine(), description.getColumn());
            this.description = description;
            this.body = body;
        }
    }
}
