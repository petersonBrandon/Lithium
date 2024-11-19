package com.lithium.cli.util;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

public class TerminalOutput {
    private final Terminal terminal;
    private static final String SEPARATOR = "════════════════════════════════════════════════════════════";
    private static final String SUB_SEPARATOR = "────────────────────────────────────────────────────────";

    public TerminalOutput(Terminal terminal) {
        this.terminal = terminal;
    }

    public void printInfo(String message) {
        terminal.writer().println(new AttributedStringBuilder()
                .append(message)
                .toAnsi());
        terminal.flush();
    }

    public void printError(String message) {
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                .append(message)
                .toAnsi());
        terminal.flush();
    }

    public void printSuccess(String message) {
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
                .append(message)
                .toAnsi());
        terminal.flush();
    }

    public void printSeparator(boolean isMain) {
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE))
                .append(isMain ? SEPARATOR : SUB_SEPARATOR)
                .toAnsi());
        terminal.flush();
    }
}