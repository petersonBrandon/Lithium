package com.lithium.cli.util;

import com.lithium.util.logger.LithiumLogger;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestFileResolver {
    private static final LithiumLogger log = LithiumLogger.getInstance();
    private final ProjectConfig config;

    public TestFileResolver(ProjectConfig config) {
        this.config = config;
    }

    public List<String> resolveTestFilePaths(String fileOrFolderName) throws IOException {
        String baseDir = config.getTestDirectory() != null ?
                config.getTestDirectory() :
                System.getProperty("user.dir");

        Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();

        // Search for exact match across all directories
        List<Path> matchedPaths = Files.walk(basePath)
                .filter(path -> {
                    String fileName = path.getFileName().toString();
                    return fileName.equals(fileOrFolderName) ||
                            fileName.equals(fileOrFolderName + ".lit");
                })
                .collect(Collectors.toList());

        if (matchedPaths.isEmpty()) {
            throw new IOException(String.format("No matching file or folder found for: %s", fileOrFolderName));
        }

        // If it's a directory, find all .lit files in that directory and subdirectories
        List<String> testFilePaths = matchedPaths.stream()
                .flatMap(matchedPath -> {
                    try {
                        if (Files.isDirectory(matchedPath)) {
                            return Files.walk(matchedPath)
                                    .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".lit"))
                                    .map(Path::toString);
                        } else if (matchedPath.toString().endsWith(".lit")) {
                            return Stream.of(matchedPath.toString());
                        }
                        return Stream.empty();
                    } catch (IOException e) {
                        log.error("Error walking directory: " + e.getMessage());
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toList());

        if (testFilePaths.isEmpty()) {
            throw new IOException(String.format("No .lit files found in or under: %s", fileOrFolderName));
        }

        return testFilePaths;
    }
}
