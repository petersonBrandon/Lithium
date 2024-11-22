package com.lithium.parser;

import com.lithium.lexer.Token;
import com.lithium.lexer.TokenType;
import com.lithium.locators.LocatorType;

import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private final int scopeDepth = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // Main parsing method
    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            try {
                statements.add(declaration());
            } catch (ParseError error) {
                synchronize();
            }
        }
        return statements;
    }

    // Handle declarations (functions, variables, etc.)
    private Stmt declaration() {
        try {
            if (match(TokenType.FUNCTION)) return function();
            if (match(TokenType.SET)) return varDeclaration();
            if (match(TokenType.IMPORT)) return importStatement();
            if (match(TokenType.EXPORT)) return exportStatement();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    // Parse function declarations
    private Stmt.Function function() {
        Token name = consume(TokenType.IDENTIFIER, "Expect function name.");
        consume(TokenType.LEFT_PAREN, "Expect '(' after function name.");

        List<Token> parameters = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Cannot have more than 255 parameters.");
                }
                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");
        consume(TokenType.LEFT_BRACE, "Expect '{' before function body.");

        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    // Parse variable declarations
    private Stmt.Var varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(TokenType.EQUALS)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    // Parse import statements
    private Stmt.Import importStatement() {
        Token path = consume(TokenType.STRING, "Expect string after import.");
        Token alias = null;

        if (match(TokenType.IDENTIFIER) && peek(-1).getLexeme().equals("as")) {
            alias = consume(TokenType.IDENTIFIER, "Expect alias name after 'as'.");
        }

        consume(TokenType.SEMICOLON, "Expect ';' after import statement.");
        return new Stmt.Import(path, alias);
    }

    // Parse export statements
    private Stmt.Export exportStatement() {
        boolean exportAll = match(TokenType.IDENTIFIER) && previous().getLexeme().equals("all");
        Token name = null;

        if (!exportAll) {
            name = consume(TokenType.IDENTIFIER, "Expect identifier after export.");
        }

        consume(TokenType.SEMICOLON, "Expect ';' after export statement.");
        return new Stmt.Export(name, exportAll);
    }

    // Parse statements
    private Stmt statement() {
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.FOR)) return forStatement();
        if (match(TokenType.RETURN)) return returnStatement();
        if (match(TokenType.TEST)) return testStatement();
        if (isCommand()) return command();
        if (match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;

        if (!check(TokenType.SEMICOLON)) {
            value = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt testStatement() {
        Token description = consume(TokenType.STRING,
                "Expect test description string.");
        consume(TokenType.LEFT_BRACE, "Expect '{' before test body.");
        List<Stmt> body = block();
        return new Stmt.Test(description, body);
    }

    // Parse if statements
    private Stmt.If ifStatement() {
        Expr condition = expression();
        consume(TokenType.LEFT_BRACE, "Expect '{' after if condition.");

        List<Stmt> thenBranch = block();
        List<Stmt> elseBranch = null;

        if (match(TokenType.ELSE)) {
            consume(TokenType.LEFT_BRACE, "Expect '{' after else.");
            elseBranch = block();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    // Parse while statements
    private Stmt.While whileStatement() {
        Expr condition = expression();
        consume(TokenType.LEFT_BRACE, "Expect '{' after while condition.");
        List<Stmt> body = block();

        return new Stmt.While(condition, body);
    }

    // Parse for statements
    private Stmt.For forStatement() {
        Token variable = consume(TokenType.IDENTIFIER, "Expect variable name in for loop.");
        consume(TokenType.IDENTIFIER, "Expect 'in' after loop variable.");

        if (!previous().getLexeme().equals("in")) {
            throw error(previous(), "Expected 'in' keyword.");
        }

        Expr range = rangeExpression();
        consume(TokenType.LEFT_BRACE, "Expect '{' after for loop range.");
        List<Stmt> body = block();

        return new Stmt.For(variable, range, body);
    }

    // Parse range expressions (e.g., 1..5)
    private Expr.Range rangeExpression() {
        Expr start = expression();
        consume(TokenType.DOT, "Expect '..' in range expression.");
        consume(TokenType.DOT, "Expect '..' in range expression.");
        Expr end = expression();

        return new Expr.Range(start, end);
    }

    // Parse commands (click, type, etc.)
    private Stmt.Command command() {
        Token command = advance();
        List<Expr> arguments = new ArrayList<>();
        Map<String, Expr> namedArgs = new HashMap<>();
        LocatorType locatorType = null;

        if (!check(TokenType.SEMICOLON)) {
            do {
                if (peek().getType() == TokenType.IDENTIFIER) {
                    // Check if this is a locator type
                    String potentialLocatorType = peek().getLexeme();
                    try {
                        locatorType = LocatorType.fromString(potentialLocatorType);
                        advance(); // consume the locator type

                        // The next token should be a string for the locator value
                        Expr locatorValue = expression();
                        arguments.add(locatorValue);
                        continue;
                    } catch (IllegalArgumentException e) {
                        // Not a locator type, treat as a named argument
                        if (peek(1).getType() == TokenType.EQUALS) {
                            Token name = advance();
                            advance(); // consume equals
                            namedArgs.put(name.getLexeme(), expression());
                            continue;
                        }
                    }
                }
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.SEMICOLON, "Expect ';' after command.");

        // Validate that commands requiring locators have them
        Stmt.Command.CommandType cmdType = Stmt.Command.CommandType.valueOf(command.getLexeme().toUpperCase());
        if (requiresLocator(cmdType) && locatorType == null) {
            throw error(command, "Command requires a locator type (e.g., id, class, link, etc.)");
        }

        return new Stmt.Command(
                Stmt.Command.CommandType.valueOf(command.getLexeme().toUpperCase()),
                command,
                arguments,
                namedArgs,
                locatorType
        );
    }

    private boolean requiresLocator(Stmt.Command.CommandType commandType) {
        return switch (commandType) {
            case CLICK, DOUBLE_CLICK, RIGHT_CLICK, HOVER, TYPE, CLEAR,
                 ASSERT_TEXT, ASSERT_VISIBLE, SELECT -> true;
            default -> false;
        };
    }

    // Parse expressions
    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = logicalOr();

        if (match(TokenType.EQUALS, TokenType.PLUS_EQUALS,
                TokenType.MINUS_EQUALS, TokenType.MULTIPLY_EQUALS,
                TokenType.DIVIDE_EQUALS)) {
            Token operator = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, operator, value);
            }

            throw error(operator, "Invalid assignment target.");
        }

        return expr;
    }

    // Parse logical OR expressions
    private Expr logicalOr() {
        Expr expr = logicalAnd();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = logicalAnd();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    // Parse logical AND expressions
    private Expr logicalAnd() {
        Expr expr = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    // Parse equality expressions
    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.EQUALS_EQUALS, TokenType.NOT_EQUALS)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // Parse comparison expressions
    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.LESS_THAN, TokenType.LESS_EQUALS,
                TokenType.GREATER_THAN, TokenType.GREATER_EQUALS)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // Parse terms (addition/subtraction)
    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // Parse factors (multiplication/division)
    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        // Handle prefix increment/decrement
        if (match(TokenType.NOT, TokenType.MINUS,
                TokenType.INCREMENT, TokenType.DECREMENT)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        Expr expr = call();

        // Handle postfix increment/decrement
        if (match(TokenType.INCREMENT, TokenType.DECREMENT)) {
            Token operator = previous();
            return new Expr.Postfix(expr, operator);
        }

        return expr;
    }

    // Parse function calls
    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(TokenType.DOT)) {
                Token name = consume(TokenType.IDENTIFIER,
                        "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    // Helper method for parsing function call arguments
    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Cannot have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }

        Token paren = consume(TokenType.RIGHT_PAREN,
                "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    // Parse primary expressions
    private Expr primary() {
        if (match(TokenType.STRING, TokenType.NUMBER))
            return new Expr.Literal(previous());

        if (match(TokenType.IDENTIFIER))
            return new Expr.Variable(previous());

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    // Helper methods for token management
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token peek(int offset) {
        return tokens.get(current + offset);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    // Error handling
    private ParseError error(Token token, String message) {
        // Report error with line and column information
        String where = token.getType() == TokenType.EOF ? " at end" :
                " at '" + token.getLexeme() + "'";
        String errorMsg = String.format("Line %d:%d - Error%s: %s",
                token.getLine(), token.getColumn(), where, message);
        throw new ParseError(errorMsg);
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().getType() == TokenType.SEMICOLON) return;

            switch (peek().getType()) {
                case FUNCTION:
                case SET:
                case IF:
                case WHILE:
                case FOR:
                case RETURN:
                case TEST:
                case IMPORT:
                case EXPORT:
                    return;
            }

            advance();
        }
    }

    private boolean isCommand() {
        TokenType type = peek().getType();
        return type == TokenType.CLICK || type == TokenType.TYPE ||
                type == TokenType.CLEAR || type == TokenType.ASSERT_TEXT ||
                type == TokenType.ASSERT_VISIBLE || type == TokenType.ASSERT_URL ||
                type == TokenType.DOUBLE_CLICK || type == TokenType.RIGHT_CLICK ||
                type == TokenType.HOVER || type == TokenType.SELECT ||
                type == TokenType.OPEN || type == TokenType.BACK ||
                type == TokenType.FORWARD || type == TokenType.REFRESH ||
                type == TokenType.SWITCH_TO_WINDOW || type == TokenType.OPEN_TAB ||
                type == TokenType.CLOSE_TAB || type == TokenType.LOG ||
                type == TokenType.WAIT;
    }

    // Custom exception for parse errors
    private static class ParseError extends RuntimeException {
        ParseError(String message) {
            super(message);
        }
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }
}