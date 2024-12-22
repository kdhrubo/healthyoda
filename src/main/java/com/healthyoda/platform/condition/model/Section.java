package com.healthyoda.platform.condition.model;

import java.util.List;

public class Section {
    private String section;
    private List<Question> questions;

    // Getters and Setters
    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
} 