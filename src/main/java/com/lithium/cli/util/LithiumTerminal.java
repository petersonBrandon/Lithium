/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: LithiumTerminal.java
 * Author: Brandon Peterson
 * Date: 11/19/2024
 * ----------------------------------------------------------------------------
 */
package com.lithium.cli.util;

import com.lithium.util.PomReader;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.IOException;

public class LithiumTerminal {
    private static final String LOGO = """
             _     _ _   _     _
            | |   (_) |_| |__ (_)_   _ _ __ ___
            | |   | | __| '_ \\| | | | | '_ ` _ \\
            | |___| | |_| | | | | |_| | | | | | |
            |_____|_|\\__|_| |_|_|\\__,_|_| |_| |_|
            """;

    private static final String SEPARATOR = "═".repeat(50);
    private static final String SUB_SEPARATOR = "─".repeat(50);

    // Spinner frames for loading animations
    private static final String[] SPINNER_FRAMES = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
    private static final String CHECK_MARK = "✓";
    private static final String CROSS_MARK = "✗";
    private static final String ARROW = "→";

    // ANSI color codes
    public static final int CYAN = 12;
    public static final int MAGENTA = 5;
    public static final int GREEN = 2;
    public static final int YELLOW = 3;
    public static final int RED = 1;

    private final Terminal terminal;
    private final LineReader lineReader;

    private static LithiumTerminal instance;

    public static LithiumTerminal getInstance() {
        if (instance == null) {
            instance = new LithiumTerminal();
        }
        return instance;
    }

    private LithiumTerminal() {
        try {
            this.terminal = TerminalBuilder.builder()
                    .system(true)
                    .dumb(true)
                    .jansi(true)
                    .build();

            this.lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .parser(new DefaultParser())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize terminal: " + e.getMessage(), e);
        }
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public LineReader getLineReader() {
        return lineReader;
    }

    public void printLogo() {
        print(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(CYAN).bold())
                .append(LOGO)
                .style(AttributedStyle.DEFAULT.foreground(MAGENTA))
                .append("Automation Framework")
                .append(" v" + new PomReader().getVersion() + "\n\n"));
    }

    public void printSeparator(boolean isMain) {
        println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT)
                .append(isMain ? SEPARATOR : SUB_SEPARATOR));
    }

    public void printStep(int stepNumber, String message) {
        println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(MAGENTA).bold())
                .append("\nStep " + stepNumber + ": ")
                .style(AttributedStyle.DEFAULT.bold())
                .append(message)
                .append("\n"));
    }

    public void printError(String message) {
        println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(RED))
                .append(message));
    }

    public void printSuccess(String message) {
        println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(GREEN))
                .append(message));
    }

    public void printInfo(String message) {
        println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT)
                .append(message));
    }

    public void showSpinner(String message, int durationMs) {
        Thread spinnerThread = new Thread(() -> {
            int frame = 0;
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < durationMs) {
                print("\r" + SPINNER_FRAMES[frame] + " " + message);
                frame = (frame + 1) % SPINNER_FRAMES.length;
                try {
                    Thread.sleep(80);
                } catch (InterruptedException e) {
                    break;
                }
            }
            println("\r" + CHECK_MARK + " " + message);
        });
        spinnerThread.start();
        try {
            spinnerThread.join();
        } catch (InterruptedException e) {
            // Handle interruption
        }
    }

    public String readLine(String prompt) {
        return lineReader.readLine(prompt);
    }

    public void println(String message) {
        terminal.writer().println(message);
        terminal.flush();
    }

    public void println(AttributedStringBuilder builder) {
        terminal.writer().println(builder.toAnsi());
        terminal.flush();
    }

    private void print(String message) {
        terminal.writer().print(message);
        terminal.flush();
    }

    private void print(AttributedStringBuilder builder) {
        terminal.writer().print(builder.toAnsi());
        terminal.flush();
    }
}
