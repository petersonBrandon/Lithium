package com.lithium.commands;

import com.lithium.commands.assertion.element.AssertVisibleCommand;
import com.lithium.commands.assertion.text.AssertTextCommand;
import com.lithium.commands.interaction.advanced.HoverCommand;
import com.lithium.commands.interaction.advanced.SelectCommand;
import com.lithium.commands.interaction.basic.*;
import com.lithium.commands.navigation.*;
import com.lithium.commands.assertion.*;
import com.lithium.commands.utility.*;
import com.lithium.commands.wait.WaitCommand;
import com.lithium.exceptions.CommandException;
import com.lithium.locators.Locator;
import com.lithium.locators.LocatorType;
import com.lithium.parser.Stmt;
import com.lithium.util.logger.LithiumLogger;

import java.util.List;

public class CommandFactory {
    private static final LithiumLogger log = LithiumLogger.getInstance();

    /**
     * Creates a Command instance based on the parsed command type and arguments
     *
     * @param commandType The type of command to create
     * @param args List of resolved positional arguments
     * @param locatorType The type of locator to use for element identification
     * @param lineNumber The line number in the source file for error reporting
     * @return A Command instance ready for execution
     * @throws CommandException if the command type is unknown or arguments are invalid
     */
    public static Command createCommand(Stmt.Command.CommandType commandType, List<String> args,
                                        LocatorType locatorType, int lineNumber) {
        return switch (commandType) {
            // UI Interactions
            case CLICK -> createClickCommand(args, locatorType, lineNumber);
            case DOUBLE_CLICK -> createDoubleClickCommand(args, locatorType, lineNumber);
            case RIGHT_CLICK -> createRightClickCommand(args, locatorType, lineNumber);
            case HOVER -> createHoverCommand(args, locatorType, lineNumber);
            case TYPE -> createTypeCommand(args, locatorType, lineNumber);
            case CLEAR -> createClearCommand(args, locatorType, lineNumber);
//            case SELECT -> createSelectCommand(args, locatorType, lineNumber);

            // Assertions
            case ASSERT_TEXT -> createAssertTextCommand(args, locatorType, lineNumber);
            case ASSERT_VISIBLE -> createAssertVisibleCommand(args, locatorType, lineNumber);
            case ASSERT_URL -> createAssertUrlCommand(args, lineNumber);

            // Navigation
            case OPEN -> createOpenCommand(args, lineNumber);
            case BACK -> createBackCommand(args, lineNumber);
            case FORWARD -> createForwardCommand(args, lineNumber);
            case REFRESH -> createRefreshCommand(args, lineNumber);
            case SWITCH_WINDOW -> createSwitchWindowCommand(args, lineNumber);
            case OPEN_TAB -> createOpenTabCommand(args, lineNumber);
            case CLOSE_TAB -> createCloseTabCommand(args, lineNumber);
            case SWITCH_TO_WINDOW -> createSwitchToWindowCommand(args, lineNumber);

            // Utility
            case LOG -> createLogCommand(args, lineNumber);
//            case WAIT -> createWaitCommand(args, lineNumber);

            default -> throw new CommandException(
                    String.format("Unknown command type '%s' at line %d", commandType, lineNumber)
            );
        };
    }

    // UI Interactions
    private static Command createClickCommand(List<String> args, LocatorType locatorType, int lineNumber) {
        validateArgCount(args, 1, "CLICK", lineNumber);
        Locator locator = new Locator(locatorType, args.getFirst());
        return new ClickCommand(locator, lineNumber);
    }

    private static Command createDoubleClickCommand(List<String> args, LocatorType locatorType, int lineNumber) {
        validateArgCount(args, 1, "DOUBLE_CLICK", lineNumber);
        Locator locator = new Locator(locatorType, args.getFirst());
        return new DoubleClickCommand(locator, lineNumber);
    }

    private static Command createRightClickCommand(List<String> args, LocatorType locatorType, int lineNumber) {
        validateArgCount(args, 1, "RIGHT_CLICK", lineNumber);
        Locator locator = new Locator(locatorType, args.getFirst());
        return new RightClickCommand(locator, lineNumber);
    }

    private static Command createHoverCommand(List<String> args, LocatorType locatorType, int lineNumber) {
        validateArgCount(args, 1, "HOVER", lineNumber);
        Locator locator = new Locator(locatorType, args.getFirst());
        return new HoverCommand(locator, lineNumber);
    }

    private static Command createTypeCommand(List<String> args, LocatorType locatorType, int lineNumber) {
        validateArgCount(args, 2, "TYPE", lineNumber);
        Locator locator = new Locator(locatorType, args.getFirst());
        return new TypeCommand(locator, args.get(1), lineNumber);
    }

    private static Command createClearCommand(List<String> args, LocatorType locatorType, int lineNumber) {
        validateArgCount(args, 1, "CLEAR", lineNumber);
        Locator locator = new Locator(locatorType, args.getFirst());
        return new ClearCommand(locator, lineNumber);
    }

//    private static Command createSelectCommand(List<String> args, LocatorType locatorType, int lineNumber) {
//        validateArgCount(args, 2, "SELECT", lineNumber);
//        Locator locator = new Locator(locatorType, args.get(0));
//        return new SelectCommand(locator, args.get(1), lineNumber);
//    }

    // Assertions
    private static Command createAssertTextCommand(List<String> args, LocatorType locatorType, int lineNumber) {
        validateArgCount(args, 2, "ASSERT_TEXT", lineNumber);
        Locator locator = new Locator(locatorType, args.getFirst());
        return new AssertTextCommand(locator, args.get(1), lineNumber);
    }

    private static Command createAssertVisibleCommand(List<String> args, LocatorType locatorType, int lineNumber) {
        validateArgCount(args, 1, "ASSERT_VISIBLE", lineNumber);
        Locator locator = new Locator(locatorType, args.getFirst());
        return new AssertVisibleCommand(locator, lineNumber);
    }

    private static Command createAssertUrlCommand(List<String> args, int lineNumber) {
        validateArgCount(args, 1, "ASSERT_URL", lineNumber);
        return new AssertURLCommand(args.getFirst(), lineNumber);
    }

    // Navigation
    private static Command createOpenCommand(List<String> args, int lineNumber) {
        validateArgCount(args, 1, "OPEN", lineNumber);
        return new OpenCommand(args.getFirst(), lineNumber);
    }

    private static Command createBackCommand(List<String> args, int lineNumber) {
        validateArgCount(args, 0, "BACK", lineNumber);
        return new BackCommand(lineNumber);
    }

    private static Command createForwardCommand(List<String> args, int lineNumber) {
        validateArgCount(args, 0, "FORWARD", lineNumber);
        return new ForwardCommand(lineNumber);
    }

    private static Command createRefreshCommand(List<String> args, int lineNumber) {
        validateArgCount(args, 0, "REFRESH", lineNumber);
        return new RefreshCommand(lineNumber);
    }

    private static Command createSwitchWindowCommand(List<String> args, int lineNumber) {
        validateArgCount(args, 1, "SWITCH_WINDOW", lineNumber);
        return new SwitchToWindowCommand(args.getFirst(), lineNumber);
    }

    private static Command createOpenTabCommand(List<String> args, int lineNumber) {
        validateArgCount(args, 1, "OPEN_TAB", lineNumber);
        return new OpenTabCommand(args.getFirst(), lineNumber);
    }

    private static Command createCloseTabCommand(List<String> args, int lineNumber) {
        validateArgCount(args, 0, "CLOSE_TAB", lineNumber);
        return new CloseTabCommand(lineNumber);
    }

    private static Command createSwitchToWindowCommand(List<String> args, int lineNumber) {
        validateArgCount(args, 1, "SWITCH_TO_WINDOW", lineNumber);
        return new SwitchToWindowCommand(args.getFirst(), lineNumber);
    }

    // Utility
    private static Command createLogCommand(List<String> args, int lineNumber) {
        validateArgCount(args, 1, "LOG", lineNumber);
        return new LogCommand(args.getFirst());
    }

//    private static Command createWaitCommand(List<String> args, int lineNumber) {
//        validateArgCount(args, 1, "WAIT", lineNumber);
//        try {
//            int milliseconds = Integer.parseInt(args.get(0));
//            return new WaitCommand(milliseconds, lineNumber);
//        } catch (NumberFormatException e) {
//            throw new CommandException(
//                    String.format("WAIT command expects a number of milliseconds, but got '%s' at line %d",
//                            args.getFirst(), lineNumber)
//            );
//        }
//    }

    private static void validateArgCount(List<String> args, int expected, String commandName, int lineNumber) {
        if (args.size() != expected) {
            throw new CommandException(
                    String.format(
                            "%s command expects %d argument(s), but got %d at line %d",
                            commandName, expected, args.size(), lineNumber
                    )
            );
        }
    }
}