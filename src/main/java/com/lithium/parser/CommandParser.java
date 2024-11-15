package com.lithium.parser;

import com.lithium.commands.*;
import com.lithium.exceptions.TestSyntaxException;
import com.lithium.locators.LocatorParser;
import com.lithium.parser.utils.LocatorUtils;
import com.lithium.parser.utils.LogUtils;
import com.lithium.parser.utils.StringUtils;
import com.lithium.parser.utils.WaitUtils;

public class CommandParser {
    public Command parseCommand(String line, int lineNumber) throws TestSyntaxException {
        String[] parts = line.trim().split("\\s+", 2);
        if (parts.length < 2) {
            throw new TestSyntaxException("Invalid command format", lineNumber);
        }

        String command = parts[0].toLowerCase();
        String remainingArgs = parts[1];

        return switch (command) {
            case "open" -> parseOpenCommand(remainingArgs, lineNumber);
            case "click" -> parseClickCommand(remainingArgs, lineNumber);
            case "type" -> parseTypeCommand(remainingArgs, lineNumber);
            case "wait" -> parseWaitCommand(remainingArgs, lineNumber);
            case "log" -> parseLogCommand(remainingArgs, lineNumber);
            case "set" -> parseSetCommand(remainingArgs, lineNumber);
            default -> null;
        };
    }

    private OpenCommand parseOpenCommand(String args, int lineNumber) throws TestSyntaxException {
        String url = StringUtils.extractQuotedString(args, lineNumber);
        return new OpenCommand(url);
    }

    private ClickCommand parseClickCommand(String args, int lineNumber) throws TestSyntaxException {
        String[] clickArgs = LocatorUtils.parseLocatorArgs(args, lineNumber);
        return new ClickCommand(LocatorParser.parse(clickArgs[0], clickArgs[1], lineNumber));
    }

    private TypeCommand parseTypeCommand(String args, int lineNumber) throws TestSyntaxException {
        String[] typeArgs = LocatorUtils.parseTypeArgs(args, lineNumber);
        return new TypeCommand(
                LocatorParser.parse(typeArgs[0], typeArgs[1], lineNumber),
                typeArgs[2]
        );
    }

    private WaitCommand parseWaitCommand(String args, int lineNumber) throws TestSyntaxException {
        String[] waitArgs = LocatorUtils.parseWaitArgs(args, lineNumber);
        String timeout = waitArgs.length > 3 ? waitArgs[3] : "30";
        return new WaitCommand(
                LocatorParser.parse(waitArgs[0], waitArgs[1], lineNumber),
                WaitUtils.parseWaitType(waitArgs[2], lineNumber),
                timeout
        );
    }

    private LogCommand parseLogCommand(String args, int lineNumber) throws TestSyntaxException {
        String[] logArgs = LogUtils.parseLogArgs(args, lineNumber);
        return LogUtils.createLogCommand(logArgs, lineNumber);
    }

    private SetCommand parseSetCommand(String args, int lineNumber) throws TestSyntaxException {
        String[] setParts = args.split("=", 2);
        if (setParts.length != 2) {
            throw new TestSyntaxException("Invalid set command format. Expected: set <variable> = <value>", lineNumber);
        }
        String varName = setParts[0].trim();
        String value = StringUtils.stripQuotes(setParts[1].trim());
        return new SetCommand(varName, value);
    }
}
