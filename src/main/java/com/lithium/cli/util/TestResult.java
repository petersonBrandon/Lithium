package com.lithium.cli.util;

import java.time.LocalDateTime;

public record TestResult(
        String fileName,
        String testName,
        ResultType result,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String errorMessage) {
}