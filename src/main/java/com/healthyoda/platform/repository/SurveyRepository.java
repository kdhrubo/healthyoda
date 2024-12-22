package com.healthyoda.platform.repository;// SurveyRepository.java

import com.healthyoda.platform.model.Survey;
import com.healthyoda.platform.model.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class SurveyRepository {
    private final JdbcClient jdbcClient;

    public SurveyRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Survey> findById(Long surveyId) {
        var sql = """
            SELECT survey_id, title, description, created_at, is_active, 
                   start_date, end_date
            FROM surveys 
            WHERE survey_id = :surveyId
            """;

        return jdbcClient.sql(sql)
                .param("surveyId", surveyId)
                .query(Survey.class)
                .optional();
    }

    public List<Question> findQuestionsBySurveyId(Long surveyId) {
        var sql = """
            SELECT question_id, survey_id, question_text, question_type, 
                   is_required, display_order
            FROM questions 
            WHERE survey_id = :surveyId 
            ORDER BY display_order
            """;

        return jdbcClient.sql(sql)
                .param("surveyId", surveyId)
                .query(Question.class)
                .list();
    }

    public List<QuestionOption> findOptionsByQuestionId(Long questionId) {
        var sql = """
            SELECT option_id, question_id, option_text, display_order
            FROM question_options 
            WHERE question_id = :questionId 
            ORDER BY display_order
            """;

        return jdbcClient.sql(sql)
                .param("questionId", questionId)
                .query(QuestionOption.class)
                .list();
    }

    public List<BranchingRule> findBranchingRulesByQuestionId(Long questionId) {
        var sql = """
            SELECT rule_id, question_id, option_id, next_question_id
            FROM branching_rules 
            WHERE question_id = :questionId
            """;

        return jdbcClient.sql(sql)
                .param("questionId", questionId)
                .query(BranchingRule.class)
                .list();
    }

    @Transactional
    public Long createSurvey(Survey survey) {
        var sql = """
            INSERT INTO surveys (title, description, is_active, start_date, end_date)
            VALUES (:title, :description, :isActive, :startDate, :endDate)
            RETURNING survey_id
            """;

        return jdbcClient.sql(sql)
                .param("title", survey.title())
                .param("description", survey.description())
                .param("isActive", survey.isActive())
                .param("startDate", survey.startDate())
                .param("endDate", survey.endDate())
                .query(Long.class)
                .single();
    }

    @Transactional
    public Long createQuestion(Question question) {
        var sql = """
            INSERT INTO questions (survey_id, question_text, question_type, 
                                 is_required, display_order)
            VALUES (:surveyId, :questionText, :questionType, :isRequired, :displayOrder)
            RETURNING question_id
            """;

        return jdbcClient.sql(sql)
                .param("surveyId", question.surveyId())
                .param("questionText", question.questionText())
                .param("questionType", question.questionType())
                .param("isRequired", question.isRequired())
                .param("displayOrder", question.displayOrder())
                .query(Long.class)
                .single();
    }

    @Transactional
    public void createResponse(Response response) {
        var sql = """
            INSERT INTO responses (survey_id, respondent_id)
            VALUES (:surveyId, :respondentId)
            RETURNING response_id
            """;

        Long responseId = jdbcClient.sql(sql)
                .param("surveyId", response.surveyId())
                .param("respondentId", response.respondentId())
                .query(Long.class)
                .single();

        for (ResponseAnswer answer : response.answers()) {
            createResponseAnswer(responseId, answer);
        }
    }

    private void createResponseAnswer(Long responseId, ResponseAnswer answer) {
        var sql = """
            INSERT INTO response_answers (response_id, question_id, option_id, answer_text)
            VALUES (:responseId, :questionId, :optionId, :answerText)
            """;

        jdbcClient.sql(sql)
                .param("responseId", responseId)
                .param("questionId", answer.questionId())
                //.param("optionId", answer.optionId())
                .param("answerText", answer.answerText())
                .update();
    }

    @Transactional
    public void createResponseAnswers(Long responseId, List<ResponseAnswer> answers) {
        for (ResponseAnswer answer : answers) {
            if (answer.hasTextAnswer()) {
                createTextAnswer(responseId, answer);
            }
            if (answer.hasOptionAnswers()) {
                createOptionAnswers(responseId, answer);
            }
        }
    }

    private void createTextAnswer(Long responseId, ResponseAnswer answer) {
        var sql = """
            INSERT INTO response_answers (response_id, question_id, answer_text)
            VALUES (:responseId, :questionId, :answerText)
            """;

        jdbcClient.sql(sql)
                .param("responseId", responseId)
                .param("questionId", answer.questionId())
                .param("answerText", answer.answerText())
                .update();
    }

    private void createOptionAnswers(Long responseId, ResponseAnswer answer) {
        var sql = """
            INSERT INTO response_answers (response_id, question_id, option_id)
            VALUES (:responseId, :questionId, :optionId)
            """;

        for (Long optionId : answer.optionIds()) {
            jdbcClient.sql(sql)
                    .param("responseId", responseId)
                    .param("questionId", answer.questionId())
                    .param("optionId", optionId)
                    .update();
        }
    }

    public List<ResponseAnswer> findAnswersByResponseId(Long responseId) {
        var sql = """
            SELECT ra.response_id, ra.question_id,
                   array_agg(ra.option_id) FILTER (WHERE ra.option_id IS NOT NULL) as option_ids,
                   string_agg(DISTINCT ra.answer_text, '') FILTER (WHERE ra.answer_text IS NOT NULL) as answer_text
            FROM response_answers ra
            WHERE ra.response_id = :responseId
            GROUP BY ra.response_id, ra.question_id
            """;

        return jdbcClient.sql(sql)
                .param("responseId", responseId)
                .query(ResponseAnswer.class)
                .list();
    }
}