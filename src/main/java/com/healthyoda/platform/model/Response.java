package com.healthyoda.platform.model;

import java.time.LocalDateTime;
import java.util.List;

public record Response(
        Long responseId,
        Long surveyId,
        Long respondentId,
        LocalDateTime submittedAt,
        List<ResponseAnswer> answers
) {
}
