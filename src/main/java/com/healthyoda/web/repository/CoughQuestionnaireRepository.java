package com.healthyoda.web.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CoughQuestionnaireRepository {
    private final ObjectMapper objectMapper;
    private Questionnaire questionnaire;
    private List<Question> allQuestions;
    private int currentQuestionIndex = 0;

    public CoughQuestionnaireRepository() {
        this.objectMapper = new ObjectMapper();
        loadQuestionnaire();
        initializeQuestionsList();
    }

    private void loadQuestionnaire() {
        try {
            Resource resource = new ClassPathResource("data/cough.json");
            questionnaire = objectMapper.readValue(resource.getInputStream(), Questionnaire.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load questionnaire", e);
        }
    }

    private void initializeQuestionsList() {
        allQuestions = questionnaire.coughAssessmentQuestionnaire().stream()
                .flatMap(section -> section.getQuestions().stream())
                .sorted(Comparator.comparingInt(Question::id))
                .collect(Collectors.toList());
    }

    public Question getNextQuestion(int questionId) {
        return this.allQuestions.stream()
                .filter(question -> question.id() >= currentQuestionIndex)
                .findFirst().orElse(null);
    }

    public Question getCurrentQuestion() {
        if (currentQuestionIndex == 0 || currentQuestionIndex > allQuestions.size()) {
            return null;
        }
        return allQuestions.get(currentQuestionIndex - 1);
    }

    public void reset() {
        currentQuestionIndex = 0;
    }

    public boolean hasMoreQuestions() {
        return currentQuestionIndex < allQuestions.size();
    }

    public int getTotalQuestions() {
        return allQuestions.size();
    }

}