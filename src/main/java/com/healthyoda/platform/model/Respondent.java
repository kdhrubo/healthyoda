package com.healthyoda.platform.model;

import java.time.LocalDateTime;

public record Respondent(
        Long respondentId,
        String email,
        LocalDateTime createdAt
) {
}
