/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: CommandArgParser.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.parser.utils;

import com.lithium.exceptions.TestSyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing command arguments in Lithium scripts.
 */
public class CommandArgParser {

    /**
     * Parses the given argument string based on the specified pattern.
     *
     * @param args       the argument string to parse
     * @param pattern    the pattern specifying the expected argument structure
     * @param lineNumber the line number in the script for error reporting
     * @return a list of parsed arguments
     * @throws TestSyntaxException if the number of arguments is invalid or the syntax is incorrect
     */
    public static List<String> parseArgs(String args, ArgPattern pattern, int lineNumber) throws TestSyntaxException {
        List<String> tokens = tokenize(args.trim(), lineNumber);

        if (tokens.size() < pattern.getMinArgs() || tokens.size() > pattern.getMaxArgs()) {
            throw new TestSyntaxException(
                    String.format("Invalid number of arguments. Expected %d-%d, got %d",
                            pattern.getMinArgs(), pattern.getMaxArgs(), tokens.size()),
                    lineNumber
            );
        }

        return tokens;
    }

    /**
     * Tokenizes the input string into a list of argument tokens.
     *
     * @param input      the input string to tokenize
     * @param lineNumber the line number in the script for error reporting
     * @return a list of tokens extracted from the input
     * @throws TestSyntaxException if there is an error in tokenization, such as an unterminated quoted string
     */
    private static List<String> tokenize(String input, int lineNumber) throws TestSyntaxException {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inConcatenation = false;
        int pos = 0;

        while (pos < input.length()) {
            char c = input.charAt(pos);

            if (Character.isWhitespace(c) && !inConcatenation) {
                addTokenIfNotEmpty(currentToken, tokens);
                pos++;
                continue;
            }

            if (c == '"') {
                // Handle quoted string
                int[] result = processQuotedString(input, pos, lineNumber);
                String quotedContent = input.substring(pos + 1, result[0]);

                if (inConcatenation) {
                    currentToken.append(quotedContent);
                } else {
                    currentToken.append(quotedContent);
                }

                pos = result[0] + 1; // Move past closing quote

                // Check for concatenation
                int nextNonWhitespace = findNextNonWhitespace(input, pos);
                if (nextNonWhitespace < input.length() && input.charAt(nextNonWhitespace) == '+') {
                    inConcatenation = true;
                    pos = nextNonWhitespace + 1;
                } else {
                    inConcatenation = false;
                    addTokenIfNotEmpty(currentToken, tokens);
                    pos = nextNonWhitespace;
                }
            } else {
                // Handle non-quoted content
                if (!inConcatenation) {
                    currentToken.append(c);
                }
                pos++;
            }
        }

        addTokenIfNotEmpty(currentToken, tokens);
        return tokens;
    }

    /**
     * Processes a quoted string starting at the given position.
     *
     * @param input      the input string
     * @param start      the starting position of the quoted string
     * @param lineNumber the line number in the script for error reporting
     * @return an array containing the position of the closing quote
     * @throws TestSyntaxException if the quoted string is unterminated
     */
    private static int[] processQuotedString(String input, int start, int lineNumber) throws TestSyntaxException {
        int end = start + 1;
        while (end < input.length()) {
            if (input.charAt(end) == '"' && input.charAt(end - 1) != '\\') {
                return new int[]{end};
            }
            end++;
        }
        throw new TestSyntaxException("Unterminated quoted string", lineNumber);
    }

    /**
     * Finds the next non-whitespace character starting from the given position.
     *
     * @param input the input string
     * @param start the starting position
     * @return the position of the next non-whitespace character
     */
    private static int findNextNonWhitespace(String input, int start) {
        int pos = start;
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
        return pos;
    }

    /**
     * Adds the current token to the list if it is not empty.
     *
     * @param token  the current token being built
     * @param tokens the list of tokens
     */
    private static void addTokenIfNotEmpty(StringBuilder token, List<String> tokens) {
        String tokenStr = token.toString().trim();
        if (!tokenStr.isEmpty()) {
            tokens.add(tokenStr);
            token.setLength(0);
        }
    }
}
