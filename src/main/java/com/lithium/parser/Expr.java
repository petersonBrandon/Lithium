package com.lithium.parser;

import com.lithium.lexer.Token;

import java.util.List;

public abstract class Expr extends Node {
    Expr(int line, int column) {
        super(line, column);
    }

    // Binary operations (e.g., a + b, a == b)
    public static class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

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
    public static class Call extends Expr {
        public final Expr callee;
        final Token paren;
        public final List<Expr> arguments;

        Call(Expr callee, Token paren, List<Expr> arguments) {
            super(paren.getLine(), paren.getColumn());
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }
    }

    // Logical operations (AND, OR)
    public static class Logical extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        Logical(Expr left, Token operator, Expr right) {
            super(operator.getLine(), operator.getColumn());
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }

    // Range expression for for-loops (e.g., 1..5)
    public static class Range extends Expr {
        public final Expr start;
        public final Expr end;

        Range(Expr start, Expr end) {
            super(start.line, start.column);
            this.start = start;
            this.end = end;
        }
    }

    // Grouping expressions (e.g., parentheses)
    public static class Grouping extends Expr {
        public final Expr expression;

        Grouping(Expr expression) {
            super(expression.line, expression.column);
            this.expression = expression;
        }
    }

    // Get expressions (for accessing properties/members)
    public static class Get extends Expr {
        public final Expr object;
        final Token name;

        Get(Expr object, Token name) {
            super(name.getLine(), name.getColumn());
            this.object = object;
            this.name = name;
        }
    }

    // Unary expressions (e.g., -x, !x)
    public static class Unary extends Expr {
        public final Token operator;
        public final Expr right;

        Unary(Token operator, Expr right) {
            super(operator.getLine(), operator.getColumn());
            this.operator = operator;
            this.right = right;
        }
    }

    public static class Assign extends Expr {
        public final Token name;
        public final Token operator;  // Now includes +=, -=, *=, /=
        public final Expr value;

        Assign(Token name, Token operator, Expr value) {
            super(name.getLine(), name.getColumn());
            this.name = name;
            this.operator = operator;
            this.value = value;
        }
    }

    public static class Postfix extends Expr {
        public final Expr operand;
        public final Token operator;

        Postfix(Expr operand, Token operator) {
            super(operator.getLine(), operator.getColumn());
            this.operand = operand;
            this.operator = operator;
        }
    }
}
