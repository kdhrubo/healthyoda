package com.healthyoda.platform.model;

import java.util.List;

public record Question(
        Long questionId,
        Long surveyId,
        String questionText,
        QuestionType questionType,
        boolean isRequired,
        int displayOrder,
        List<QuestionOption> options,
        List<BranchingRule> branchingRules,
        QuestionValidation validation
) {
    public boolean requiresTextValidation() {
        return questionType == QuestionType.TEXT ||
                questionType == QuestionType.EMAIL ||
                questionType == QuestionType.PHONE ||
                questionType == QuestionType.URL;
    }

    public boolean requiresNumericValidation() {
        return questionType == QuestionType.NUMBER;
    }

    public boolean allowsMultipleAnswers() {
        return questionType == QuestionType.MULTI_CHOICE;
    }
}

