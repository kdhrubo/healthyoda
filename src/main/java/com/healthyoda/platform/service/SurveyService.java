// SurveyService.java
package com.healthyoda.platform.service;

import com.healthyoda.platform.model.Question;
import com.healthyoda.platform.model.Response;
import com.healthyoda.platform.model.ResponseAnswer;
import com.healthyoda.platform.model.Survey;
import com.healthyoda.platform.repository.SurveyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class SurveyService {
    private final SurveyRepository surveyRepository;

    public SurveyService(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    public Optional<Survey> getSurveyWithQuestions(Long surveyId) {
        return surveyRepository.findById(surveyId)
            .map(survey -> {
                var questions = surveyRepository.findQuestionsBySurveyId(surveyId)
                    .stream()
                    .map(this::enrichQuestionWithOptions)
                    .toList();
                
                return new Survey(
                    survey.surveyId(),
                    survey.title(),
                    survey.description(),
                    survey.createdAt(),
                    survey.isActive(),
                    survey.startDate(),
                    survey.endDate(),
                    questions
                );
            });
    }

    private Question enrichQuestionWithOptions(Question question) {
        var options = surveyRepository.findOptionsByQuestionId(question.questionId());
        var branchingRules = surveyRepository.findBranchingRulesByQuestionId(question.questionId());
        
        return new Question(
            question.questionId(),
            question.surveyId(),
            question.questionText(),
            question.questionType(),
            question.isRequired(),
            question.displayOrder(),
            options,
            branchingRules,null
        );
    }

    @Transactional
    public Long createSurvey(Survey survey) {
        Long surveyId = surveyRepository.createSurvey(survey);
        
        if (survey.questions() != null) {
            for (Question question : survey.questions()) {
                createQuestion(surveyId, question);
            }
        }
        
        return surveyId;
    }

    private void createQuestion(Long surveyId, Question question) {
        var questionWithSurveyId = new Question(
            null,
            surveyId,
            question.questionText(),
            question.questionType(),
            question.isRequired(),
            question.displayOrder(),
            question.options(),
            question.branchingRules(), null
        );
        
        surveyRepository.createQuestion(questionWithSurveyId);
    }

    public Question getNextQuestion(Long currentQuestionId, Long selectedOptionId) {
        var branchingRules = surveyRepository.findBranchingRulesByQuestionId(currentQuestionId);
        
        return branchingRules.stream()
            .filter(rule -> rule.optionId().equals(selectedOptionId))
            .findFirst()
            .map(rule -> surveyRepository.findQuestionsBySurveyId(rule.nextQuestionId())
                .stream()
                .findFirst()
                .map(this::enrichQuestionWithOptions)
                .orElse(null))
            .orElse(null);
    }

    @Transactional
    public void submitResponse(Response response) {
        validateResponse(response);
        surveyRepository.createResponse(response);
    }

    private void validateResponse(Response response) {
        for (ResponseAnswer answer : response.answers()) {
            Question question = surveyRepository.findQuestionsBySurveyId(response.surveyId())
                    .stream()
                    .filter(q -> q.questionId().equals(answer.questionId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Question not found"));

            validateAnswer(question, answer);
        }
    }

    private void validateAnswer(Question question, ResponseAnswer answer) {
        switch (question.questionType()) {
            case TEXT -> validateTextAnswer(question, answer);
            case SINGLE_CHOICE -> validateSingleChoiceAnswer(question, answer);
            case MULTI_CHOICE -> validateMultiChoiceAnswer(question, answer);
        }
    }

    private void validateTextAnswer(Question question, ResponseAnswer answer) {
        if (question.isRequired() && !answer.hasTextAnswer()) {
            throw new IllegalArgumentException("Text answer required for question: " + question.questionId());
        }
        if (answer.hasOptionAnswers()) {
            throw new IllegalArgumentException("Text question cannot have option answers");
        }
    }

    private void validateSingleChoiceAnswer(Question question, ResponseAnswer answer) {
        if (!answer.hasOptionAnswers()) {
            if (question.isRequired()) {
                throw new IllegalArgumentException("Option selection required for question: " + question.questionId());
            }
            return;
        }

        if (answer.optionIds().size() != 1) {
            throw new IllegalArgumentException("Single choice question must have exactly one answer");
        }

        validateOptionBelongsToQuestion(question, answer.optionIds().get(0));
    }

    private void validateMultiChoiceAnswer(Question question, ResponseAnswer answer) {
        if (question.isRequired() && !answer.hasOptionAnswers()) {
            throw new IllegalArgumentException("At least one option required for question: " + question.questionId());
        }

        if (answer.hasOptionAnswers()) {
            answer.optionIds().forEach(optionId ->
                    validateOptionBelongsToQuestion(question, optionId));
        }
    }

    private void validateOptionBelongsToQuestion(Question question, Long optionId) {
        boolean validOption = question.options().stream()
                .anyMatch(option -> option.optionId().equals(optionId));

        if (!validOption) {
            throw new IllegalArgumentException(
                    "Option " + optionId + " does not belong to question " + question.questionId());
        }
    }

    public Question getNextQuestion(Long currentQuestionId, List<Long> selectedOptionIds) {
        // For single-choice questions, use the first (and only) option
        // For multi-choice questions, find the first matching rule
        return selectedOptionIds.stream()
                .map(optionId -> getNextQuestionForOption(currentQuestionId, optionId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(null);
    }

    private Optional<Question> getNextQuestionForOption(Long currentQuestionId, Long selectedOptionId) {
        var branchingRules = surveyRepository.findBranchingRulesByQuestionId(currentQuestionId);

        return branchingRules.stream()
                .filter(rule -> rule.optionId().equals(selectedOptionId))
                .findFirst()
                .map(rule -> surveyRepository.findQuestionsBySurveyId(rule.nextQuestionId())
                        .stream()
                        .findFirst()
                        .map(this::enrichQuestionWithOptions)
                        .orElse(null));
    }

}