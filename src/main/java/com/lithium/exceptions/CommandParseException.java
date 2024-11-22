/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: CommandParseException.java
 * Author: Brandon Peterson
 * Date: 11/21/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.exceptions;

/**
 * Exception thrown when there is an error parsing a command string into a Command object.
 * This can occur due to invalid syntax, missing required arguments, or unknown command types.
 */
public class CommandParseException extends RuntimeException {
  private final String command;
  private final int lineNumber;

  /**
   * Constructs a CommandParseException with a message and the problematic command string.
   *
   * @param message The error message
   * @param command The command string that caused the error
   * @param lineNumber The line number where the parsing error occurred
   */
  public CommandParseException(String message, String command, int lineNumber) {
    super(String.format("Line %d: %s (Command: '%s')", lineNumber, message, command));
    this.command = command;
    this.lineNumber = lineNumber;
  }

  /**
   * Constructs a CommandParseException with just a message.
   * Useful for general parsing errors not tied to a specific command.
   *
   * @param message The error message
   */
  public CommandParseException(String message) {
    super(message);
    this.command = null;
    this.lineNumber = -1;
  }

  /**
   * Gets the command string that caused the parsing error.
   *
   * @return The command string, or null if not specified
   */
  public String getCommand() {
    return command;
  }

  /**
   * Gets the line number where the parsing error occurred.
   *
   * @return The line number, or -1 if not specified
   */
  public int getLineNumber() {
    return lineNumber;
  }
}
