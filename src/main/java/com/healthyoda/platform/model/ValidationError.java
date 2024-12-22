package com.healthyoda.platform.model;

public record ValidationError(
    Long questionId,
    String message
) {}