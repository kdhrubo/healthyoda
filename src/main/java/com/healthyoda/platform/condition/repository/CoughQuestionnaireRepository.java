package com.healthyoda.platform.condition.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthyoda.platform.condition.model.Question;
import com.healthyoda.platform.condition.model.Questionnaire;
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


    public CoughQuestionnaireRepository() {
        this.objectMapper = new ObjectMapper();
        loadQuestionnaire();
        initializeQuestionsList();
    }

    public List<Question> getAllQuestions() {
        return allQuestions;
    }

    private void loadQuestionnaire() {
        try {
            Resource resource = new ClassPathResource("data/cough-short.json");
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
                .filter(question -> question.id() >= questionId)
                .findFirst().orElse(null);
    }


    public int getTotalQuestions() {
        return allQuestions.size();
    }

}