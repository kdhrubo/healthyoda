package com.healthyoda.platform.model;

import java.util.List;

public record QuestionOption(
        Long optionId,
        Long questionId,
        String optionText,
        int displayOrder,
        boolean requiresText,          // For SELECT_ONE_TEXT
        String textPrompt,            // Prompt for additional text
        List<Long> mutuallyExclusive, // Options that can't be selected together
        List<Long> dependencies,      // Options that must be selected together
        QuestionValidation textValidation  // Validation for additional text
) {}