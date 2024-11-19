package com.lithium.cli.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;

public class TestFileResolver {
    private final ProjectConfig config;

    public TestFileResolver(ProjectConfig config) {
        this.config = config;
    }

    public String resolveTestFilePath(String fileName) throws IOException {
        String baseDir = config.getTestDirectory() != null ?
                config.getTestDirectory() :
                System.getProperty("user.dir");

        Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
        String fullFileName = fileName.endsWith(".lit") ? fileName : fileName + ".lit";

        Path directPath = basePath.resolve(fullFileName);
        if (Files.exists(directPath)) {
            return directPath.toString();
        }

        if (config.getTestDirectory() != null) {
            try {
                return Files.walk(basePath)
                        .filter(path -> path.getFileName().toString().equals(fullFileName))
                        .findFirst()
                        .orElseThrow(() -> new FileNotFoundException(
                                String.format("Test file '%s' not found in directory '%s' or its subdirectories",
                                        fullFileName, baseDir)
                        ))
                        .toString();
            } catch (IOException e) {
                throw new IOException("Error searching for test file: " + e.getMessage(), e);
            }
        }

        return directPath.toString();
    }
}
