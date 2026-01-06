package com.example.starchart.api;

import java.time.Instant;

public record ErrorResponse(
    String error,
    String message,
    String path,
    Instant timestamp
) {}