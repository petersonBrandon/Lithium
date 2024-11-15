/*
 * ----------------------------------------------------------------------------
 * Project: Lithium Automation Framework
 * File: LithiumCommand.java
 * Author: Brandon Peterson
 * Date: 11/15/2024
 * ----------------------------------------------------------------------------
 */

package com.lithium.cli;

/**
 * Command interface
 */
public interface LithiumCommand {
    void execute(String[] args);
    String getDescription();
    String getUsage();
}
