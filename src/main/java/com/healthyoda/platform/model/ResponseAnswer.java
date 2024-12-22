package com.healthyoda.platform.model;

import java.util.List;
import java.util.Map;

public record ResponseAnswer(
        Long answerId,
        Long responseId,
        Long questionId,
        List<Long> optionIds,
        String answerText,
        Map<Long, String> optionTexts      // For TEXT questions
) {
    public boolean hasOptionAnswers() {
        return optionIds != null && !optionIds.isEmpty();
    }

    public boolean hasTextAnswer() {
        return answerText != null && !answerText.isBlank();
    }

}
