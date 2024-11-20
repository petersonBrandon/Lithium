package com.lithium.cli.util;

import java.util.HashMap;
import java.util.Map;

public class CommandLineArgsParser {
    private final ProjectConfig config;

    public CommandLineArgsParser(ProjectConfig config) {
        this.config = config;
    }

    public Map<String, String> parseArgs(String[] args) {
        Map<String, String> parsedArgs = new HashMap<>();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String[] parts = arg.substring(2).split("=", 2);
                if (parts.length == 2) {
                    parsedArgs.put(parts[0], parts[1]);
                } else {
                    parsedArgs.put(parts[0], "true");
                }
            }
        }
        return parsedArgs;
    }

    public boolean getBooleanOption(Map<String, String> cliArgs, String key, boolean defaultValue) {
        if (!config.canCliOverride()) return defaultValue;
        String value = cliArgs.get(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    public String getStringOption(Map<String, String> cliArgs, String key, String defaultValue) {
        if (!config.canCliOverride()) return defaultValue;
        return cliArgs.getOrDefault(key, defaultValue);
    }

    public int getIntOption(Map<String, String> cliArgs, String key, int defaultValue) {
        if (!config.canCliOverride()) return defaultValue;
        String value = cliArgs.get(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean argExists(Map<String, String> cliArgs, String key) {
        String value = cliArgs.get(key);
        return value != null;
    }
}
