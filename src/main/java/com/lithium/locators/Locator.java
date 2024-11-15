/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: Locator.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.locators;

import org.openqa.selenium.By;

/**
 * The Locator class represents a way to locate a web element on a page using a specific type and value.
 * It provides a method to convert the locator into a Selenium-friendly `By` object for use in WebDriver actions.
 */
public class Locator {
    private final LocatorType type;
    private final String value;

    /**
     * Constructs a Locator with the specified type and value.
     *
     * @param type  The type of the locator (e.g., CSS, XPATH, ID).
     * @param value The value of the locator (e.g., the CSS selector or XPath expression).
     */
    public Locator(LocatorType type, String value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Converts the Locator to a Selenium `By` object based on its type.
     *
     * @return A Selenium `By` object that can be used to locate elements using WebDriver.
     */
    public By toSeleniumBy() {
        return switch (type) {
            case CSS -> By.cssSelector(value);
            case XPATH -> By.xpath(value);
            case ID -> By.id(value);
            case NAME -> By.name(value);
            case CLASS_NAME -> By.className(value);
            case LINK_TEXT -> By.linkText(value);
            case PARTIAL_LINK_TEXT -> By.partialLinkText(value);
            case TAG_NAME -> By.tagName(value);
        };
    }

    /**
     * Returns a string representation of the Locator in the format:
     * <locator type prefix> "<locator value>"
     *
     * @return A string representation of the Locator.
     */
    @Override
    public String toString() {
        return type.prefix + " \"" + value + "\"";
    }

    /**
     * Get the type of locator
     *
     * @return LocatorType
     */
    public LocatorType getType() {
        return type;
    }

    /**
     * Get the value of a locator
     *
     * @return the locator value
     */
    public String getValue() {
        return value;
    }
}
