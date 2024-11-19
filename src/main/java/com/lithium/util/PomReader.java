package com.lithium.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PomReader {

    private final Properties properties = new Properties();

    public PomReader() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("pom-info.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                throw new RuntimeException("pom-info.properties file not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load POM info: " + e.getMessage(), e);
        }
    }

    public String getGroupId() {
        return properties.getProperty("groupId");
    }

    public String getArtifactId() {
        return properties.getProperty("artifactId");
    }

    public String getVersion() {
        return properties.getProperty("version");
    }

    public String getName() {
        return properties.getProperty("name");
    }

    public Map<String, String> getAllProperties() {
        Map<String, String> allProperties = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            allProperties.put(key, properties.getProperty(key));
        }
        return allProperties;
    }
}
