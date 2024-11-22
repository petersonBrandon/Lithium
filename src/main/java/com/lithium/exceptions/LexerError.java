package com.lithium.exceptions;

public class LexerError extends RuntimeException {
    private final int line;
    private final int column;
    private final String message;

    public LexerError(int line, int column, String message) {
        super(String.format("Error at line %d, column %d: %s", line, column, message));
        this.line = line;
        this.column = column;
        this.message = message;
    }

    public int getLine() { return line; }
    public int getColumn() { return column; }
    public String getErrorMessage() { return message; }
}
