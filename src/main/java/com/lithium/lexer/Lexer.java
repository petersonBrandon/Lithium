package com.lithium.lexer;

import com.lithium.exceptions.LexerError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int column = 1;

    private static final Map<String, TokenType> keywords;
    private static final Map<String, TokenType> commands;

    static {
        keywords = new HashMap<>();
        // Basic keywords
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("while", TokenType.WHILE);
        keywords.put("import", TokenType.IMPORT);
        keywords.put("export", TokenType.EXPORT);
        keywords.put("set", TokenType.SET);
        keywords.put("function", TokenType.FUNCTION);
        keywords.put("return", TokenType.RETURN);
        keywords.put("test", TokenType.TEST);

        commands = new HashMap<>();
        // Assertion commands
        commands.put("assertText", TokenType.ASSERT_TEXT);
        commands.put("assertVisible", TokenType.ASSERT_VISIBLE);
        commands.put("assertURL", TokenType.ASSERT_URL);

        // Interaction commands
        commands.put("click", TokenType.CLICK);
        commands.put("type", TokenType.TYPE);
        commands.put("clear", TokenType.CLEAR);
        commands.put("doubleClick", TokenType.DOUBLE_CLICK);
        commands.put("rightClick", TokenType.RIGHT_CLICK);
        commands.put("hover", TokenType.HOVER);
        commands.put("select", TokenType.SELECT);

        // Navigation commands
        commands.put("open", TokenType.OPEN);
        commands.put("back", TokenType.BACK);
        commands.put("forward", TokenType.FORWARD);
        commands.put("refresh", TokenType.REFRESH);
        commands.put("switchToWindow", TokenType.SWITCH_TO_WINDOW);
        commands.put("openTab", TokenType.OPEN_TAB);
        commands.put("closeTab", TokenType.CLOSE_TAB);

        // Utility commands
        commands.put("log", TokenType.LOG);
        commands.put("wait", TokenType.WAIT);
    }

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        try {
            while (!isAtEnd()) {
                start = current;
                scanToken();
            }

            tokens.add(new Token(TokenType.EOF, "", null, line, column));
            return tokens;
        } catch (LexerError e) {
            throw e;
        } catch (Exception e) {
            throw new LexerError(line, column, "Unexpected error: " + e.getMessage());
        }
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // Basic symbols
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case '[' -> addToken(TokenType.LEFT_BRACKET);
            case ']' -> addToken(TokenType.RIGHT_BRACKET);
            case ',' -> addToken(TokenType.COMMA);
            case '.' -> addToken(TokenType.DOT);
            case ';' -> addToken(TokenType.SEMICOLON);

            // Operators
            case '+' -> {
                if (match('+')) addToken(TokenType.INCREMENT);
                else if (match('=')) addToken(TokenType.PLUS_EQUALS);
                else addToken(TokenType.PLUS);
            }
            case '-' -> {
                if (match('-')) addToken(TokenType.DECREMENT);
                else if (match('=')) addToken(TokenType.MINUS_EQUALS);
                else addToken(TokenType.MINUS);
            }
            case '*' -> {
                if (match('=')) addToken(TokenType.MULTIPLY_EQUALS);
                else addToken(TokenType.MULTIPLY);
            }
            case '/' -> {
                if (match('/')) {
                    singleLineComment();
                } else if (match('*')) {
                    multiLineComment();
                } else if (match('=')) {
                    addToken(TokenType.DIVIDE_EQUALS);
                } else {
                    addToken(TokenType.DIVIDE);
                }
            }
            case '=' -> addToken(match('=') ? TokenType.EQUALS_EQUALS : TokenType.EQUALS);
            case '!' -> addToken(match('=') ? TokenType.NOT_EQUALS : TokenType.NOT);
            case '<' -> addToken(match('=') ? TokenType.LESS_EQUALS : TokenType.LESS_THAN);
            case '>' -> addToken(match('=') ? TokenType.GREATER_EQUALS : TokenType.GREATER_THAN);
            case '&' -> {
                if (match('&')) addToken(TokenType.AND);
                else throw new LexerError(line, column, "Expected '&' after '&'");
            }
            case '|' -> {
                if (match('|')) addToken(TokenType.OR);
                else throw new LexerError(line, column, "Expected '|' after '|'");
            }

            // Whitespace
            case ' ', '\r', '\t' -> column++;
            case '\n' -> {
                line++;
                column = 1;
            }

            // String literals
            case '"' -> string();
            case '\'' -> singleQuoteString();

            default -> {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    throw new LexerError(line, column,
                            "Unexpected character: '" + c + "'");
                }
            }
        }
    }

    // Enhanced string handling with escape sequences
    private void string() {
        StringBuilder value = new StringBuilder();
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                column = 1;
            }
            if (peek() == '\\') {
                advance(); // consume backslash
                char next = advance();
                switch (next) {
                    case 'n' -> value.append('\n');
                    case 't' -> value.append('\t');
                    case 'r' -> value.append('\r');
                    case 'b' -> value.append('\b');
                    case 'f' -> value.append('\f');
                    case '\\' -> value.append('\\');
                    case '"' -> value.append('"');
                    default -> throw new LexerError(line, column,
                            "Invalid escape sequence: \\" + next);
                }
            } else {
                value.append(advance());
            }
        }

        if (isAtEnd()) {
            throw new LexerError(line, column, "Unterminated string");
        }

        advance(); // closing "
        addToken(TokenType.STRING, value.toString());
    }

    private void singleQuoteString() {
        StringBuilder value = new StringBuilder();
        while (peek() != '\'' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                column = 1;
            }
            if (peek() == '\\') {
                advance(); // consume backslash
                char next = advance();
                switch (next) {
                    case 'n' -> value.append('\n');
                    case 't' -> value.append('\t');
                    case 'r' -> value.append('\r');
                    case 'b' -> value.append('\b');
                    case 'f' -> value.append('\f');
                    case '\\' -> value.append('\\');
                    case '\'' -> value.append('\'');  // Note: handling single quote escape
                    default -> throw new LexerError(line, column,
                            "Invalid escape sequence: \\" + next);
                }
            } else {
                value.append(advance());
            }
        }

        if (isAtEnd()) {
            throw new LexerError(line, column, "Unterminated string");
        }

        advance(); // closing '
        addToken(TokenType.STRING, value.toString());
    }

    // Single line comment handling
    private void singleLineComment() {
        while (peek() != '\n' && !isAtEnd()) {
            advance();
        }
    }

    // Multi-line comment handling
    private void multiLineComment() {
        while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance(); // *
                advance(); // /
                return;
            }
            if (peek() == '\n') {
                line++;
                column = 1;
            }
            advance();
        }

        if (isAtEnd()) {
            throw new LexerError(line, column,
                    "Unterminated multi-line comment");
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current).toLowerCase();

        // Check for keywords first
        TokenType type = keywords.get(text);
        if (type == null) {
            // Check for commands
            type = commands.get(text);
            if (type == null) {
                type = TokenType.IDENTIFIER;
            }
        }

        addToken(type);
    }

    // Enhanced number handling with scientific notation
    private void number() {
        while (isDigit(peek())) advance();

        // Look for decimal point
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // consume .
            while (isDigit(peek())) advance();
        }

        // Look for scientific notation
        if (peek() == 'e' || peek() == 'E') {
            advance(); // consume e/E
            if (peek() == '+' || peek() == '-') advance();
            if (!isDigit(peek())) {
                throw new LexerError(line, column,
                        "Invalid scientific notation");
            }
            while (isDigit(peek())) advance();
        }

        addToken(TokenType.NUMBER,
                Double.parseDouble(source.substring(start, current)));
    }

    // Helper methods remain mostly the same, but with column tracking added
    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        column++;
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line, column));
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
