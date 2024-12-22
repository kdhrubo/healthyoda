package com.healthyoda.platform.model;

public record BranchingRule(
        Long ruleId,
        Long questionId,
        Long optionId,
        Long nextQuestionId
) {
}
