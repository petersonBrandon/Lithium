/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: LocatorType.java
 * Author: Brandon Peterson
 * Date: 11/13/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.locators;

/**
 * The LocatorType enum defines the various types of locators that can be used to locate elements in a web page.
 * Each type has a corresponding prefix used in the locator string (e.g., "css", "xpath").
 */
public enum LocatorType {
    CSS("css"),
    XPATH("xpath"),
    ID("id"),
    NAME("name"),
    CLASS_NAME("class"),
    LINK_TEXT("link"),
    PARTIAL_LINK_TEXT("partialLink"),
    TAG_NAME("tag");

    public final String value;

    /**
     * Constructor for the LocatorType enum.
     *
     * @param prefix The prefix associated with the locator type (e.g., "css", "xpath").
     */
    LocatorType(String prefix) {
        this.value = prefix;
    }

    /**
     * Converts a locator prefix string to its corresponding LocatorType.
     *
     * @param prefix The prefix string representing a locator type (e.g., "css", "xpath").
     * @return The corresponding LocatorType for the given prefix.
     * @throws IllegalArgumentException If the prefix does not match any known LocatorType.
     */
    public static LocatorType fromPrefix(String prefix) {
        for (LocatorType type : values()) {
            if (type.value.equalsIgnoreCase(prefix)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown locator type: " + prefix);
    }

    public static LocatorType fromString(String type) {
        for (LocatorType locatorType : values()) {
            if (locatorType.value.equalsIgnoreCase(type)) {
                return locatorType;
            }
        }
        throw new IllegalArgumentException("Unknown locator type: " + type);
    }
}
