package com.healthyoda.platform.condition.model;

public record TranscribedQuestionAnswer(String question, String answer) {

    public String getQA() {
        return "Question: " + question + " Answer: " + answer;
    }

}
