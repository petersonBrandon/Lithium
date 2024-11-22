/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: SelectCommand.java
 * Author: [Your Name]
 * Date: 11/20/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.commands.interaction.advanced;

import com.lithium.commands.Command;
import com.lithium.core.TestContext;
import com.lithium.exceptions.CommandException;
import com.lithium.locators.Locator;
import com.lithium.util.logger.LithiumLogger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * The SelectCommand class represents a command to select an option from a dropdown element.
 * This command supports selection by visible text, value attribute, and index.
 */
public class SelectCommand {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private Locator locator;
    private final String optionText;
    private final int index;
    private final SelectionType selectionType;
    private final int lineNumber;

    /**
     * Enum to specify the type of selection to be performed.
     */
    public enum SelectionType {
        TEXT,
        VALUE,
        INDEX
    }

    /**
     * Constructs a SelectCommand for selection by visible text or value.
     *
     * @param locator The Locator used to find the select element.
     * @param optionText The text or value to select.
     * @param selectionType The type of selection (BY_VISIBLE_TEXT or BY_VALUE).
     * @param lineNumber The line number where this command appears in the test script.
     */
    public SelectCommand(Locator locator, String optionText, SelectionType selectionType, int lineNumber) {
        this.locator = locator;
        this.optionText = optionText;
        this.index = -1;
        this.selectionType = selectionType;
        this.lineNumber = lineNumber;

        if (selectionType == SelectionType.INDEX) {
            throw new IllegalArgumentException("Use the index constructor for BY_INDEX selection type");
        }
    }

    /**
     * Constructs a SelectCommand for selection by index.
     *
     * @param locator The Locator used to find the select element.
     * @param index The index of the option to select (0-based).
     * @param lineNumber The line number where this command appears in the test script.
     */
    public SelectCommand(Locator locator, int index, int lineNumber) {
        this.locator = locator;
        this.optionText = null;
        this.index = index;
        this.selectionType = SelectionType.INDEX;
        this.lineNumber = lineNumber;
    }

    /**
     * Executes the select action on the dropdown element identified by the locator.
     *
     * @param driver The WebDriver instance used to interact with the web page.
     * @param wait The WebDriverWait instance used to wait for the element.
     * @param context The TestContext instance containing test execution context.
     */

    public void execute(WebDriver driver, WebDriverWait wait, TestContext context) {
        try {
            locator = new Locator(locator.getType(), context.resolveVariables(locator.getValue()));
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator.toSeleniumBy()));
            Select select = new Select(element);

            switch (selectionType) {
                case TEXT:
                    String resolvedText = context.resolveVariables(optionText);
                    select.selectByVisibleText(resolvedText);
                    log.info(String.format("Selected option with text '%s' from dropdown: %s",
                            resolvedText, locator));
                    break;

                case VALUE:
                    String resolvedValue = context.resolveVariables(optionText);
                    select.selectByValue(resolvedValue);
                    log.info(String.format("Selected option with value '%s' from dropdown: %s",
                            resolvedValue, locator));
                    break;

                case INDEX:
                    select.selectByIndex(index);
                    log.info(String.format("Selected option at index '%d' from dropdown: %s",
                            index, locator));
                    break;
            }

        } catch (NoSuchElementException e) {
            String optionIdentifier = selectionType == SelectionType.INDEX ?
                    String.valueOf(index) : optionText;
            throw new CommandException(String.format(
                    "Line %s: Select element or option not found: '%s %s', Option: '%s'",
                    lineNumber,
                    locator.getType(),
                    locator.getValue(),
                    optionIdentifier
            ));
        } catch (StaleElementReferenceException e) {
            throw new CommandException(String.format(
                    "Line %s: Stale element reference for select element: '%s %s'",
                    lineNumber,
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (TimeoutException e) {
            throw new CommandException(String.format(
                    "Line %s: Timeout waiting for select element: '%s %s'",
                    lineNumber,
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (UnsupportedOperationException e) {
            throw new CommandException(String.format(
                    "Line %s: Element '%s %s' is not a select element",
                    lineNumber,
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (IndexOutOfBoundsException e) {
            throw new CommandException(String.format(
                    "Line %s: Index %d is out of bounds for select element: '%s %s'",
                    lineNumber,
                    index,
                    locator.getType(),
                    locator.getValue()
            ));
        } catch (Exception e) {
            String optionIdentifier = selectionType == SelectionType.INDEX ?
                    String.valueOf(index) : optionText;
            throw new CommandException(String.format(
                    "Line %s: Unable to select option '%s' from element: '%s %s'",
                    lineNumber,
                    optionIdentifier,
                    locator.getType(),
                    locator.getValue()
            ));
        }
    }
}