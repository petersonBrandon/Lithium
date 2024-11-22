package com.lithium.parser;

import com.lithium.lexer.Token;

import java.util.List;

public abstract class Expr extends Node {
    Expr(int line, int column) {
        super(line, column);
    }

    // Binary operations (e.g., a + b, a == b)
    static class Binary extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        Binary(Expr left, Token operator, Expr right) {
            super(operator.getLine(), operator.getColumn());
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }

    // Variable references
    public static class Variable extends Expr {
        public final Token name;

        Variable(Token name) {
            super(name.getLine(), name.getColumn());
            this.name = name;
        }
    }

    // Literal values (strings, numbers, etc.)
    public static class Literal extends Expr {
        public final Object value;

        Literal(Token token) {
            super(token.getLine(), token.getColumn());
            this.value = token.getLiteral();
        }
    }

    // Function calls
    static class Call extends Expr {
        final Expr callee;
        final Token paren;
        final List<Expr> arguments;

        Call(Expr callee, Token paren, List<Expr> arguments) {
            super(paren.getLine(), paren.getColumn());
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }
    }

    // Logical operations (AND, OR)
    static class Logical extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        Logical(Expr left, Token operator, Expr right) {
            super(operator.getLine(), operator.getColumn());
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }

    // Range expression for for-loops (e.g., 1..5)
    static class Range extends Expr {
        final Expr start;
        final Expr end;

        Range(Expr start, Expr end) {
            super(start.line, start.column);
            this.start = start;
            this.end = end;
        }
    }

    // Grouping expressions (e.g., parentheses)
    static class Grouping extends Expr {
        final Expr expression;

        Grouping(Expr expression) {
            super(expression.line, expression.column);
            this.expression = expression;
        }
    }

    // Get expressions (for accessing properties/members)
    static class Get extends Expr {
        final Expr object;
        final Token name;

        Get(Expr object, Token name) {
            super(name.getLine(), name.getColumn());
            this.object = object;
            this.name = name;
        }
    }

    // Unary expressions (e.g., -x, !x)
    static class Unary extends Expr {
        final Token operator;
        final Expr right;

        Unary(Token operator, Expr right) {
            super(operator.getLine(), operator.getColumn());
            this.operator = operator;
            this.right = right;
        }
    }

    static class Assign extends Expr {
        final Token name;
        final Token operator;  // Now includes +=, -=, *=, /=
        final Expr value;

        Assign(Token name, Token operator, Expr value) {
            super(name.getLine(), name.getColumn());
            this.name = name;
            this.operator = operator;
            this.value = value;
        }
    }

    static class Postfix extends Expr {
        final Expr operand;
        final Token operator;

        Postfix(Expr operand, Token operator) {
            super(operator.getLine(), operator.getColumn());
            this.operand = operand;
            this.operator = operator;
        }
    }
}
