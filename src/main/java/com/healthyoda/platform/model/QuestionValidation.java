package com.healthyoda.platform.model;

public record QuestionValidation(
    Integer minLength,
    Integer maxLength,
    String regex,
    Double minValue,
    Double maxValue,
    String errorMessage
) {}
