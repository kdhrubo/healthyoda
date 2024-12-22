// Survey.java
package com.healthyoda.platform.model;


import java.time.LocalDateTime;
import java.util.List;

public record Survey(
    Long surveyId,
    String title,
    String description,
    LocalDateTime createdAt,
    boolean isActive,
    LocalDateTime startDate,
    LocalDateTime endDate,
    List<Question> questions
) {}



