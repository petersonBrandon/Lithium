package com.lithium.parser;

public abstract class Node {
    public final int line;
    public final int column;

    Node(int line, int column) {
        this.line = line;
        this.column = column;
    }
}
