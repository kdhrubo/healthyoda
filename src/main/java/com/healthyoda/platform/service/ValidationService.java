// ValidationService.java
package com.healthyoda.platform.service;

import com.healthyoda.platform.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class ValidationService {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    private static final Pattern URL_PATTERN = 
        Pattern.compile("^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$");

    public List<ValidationError> validateAnswer(Question question, ResponseAnswer answer) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Required field validation
        if (question.isRequired() && isEmptyAnswer(answer)) {
            errors.add(new ValidationError(
                question.questionId(),
                "This question is required"
            ));
            return errors;
        }

        // Type-specific validation
        switch (question.questionType()) {
            case TEXT -> validateTextAnswer(question, answer, errors);
            case SINGLE_CHOICE -> validateSingleChoice(question, answer, errors);
            case MULTI_CHOICE -> validateMultiChoice(question, answer, errors);
            case SELECT_ONE_TEXT -> validateSelectOneText(question, answer, errors);
            case EMAIL -> validateEmail(question, answer, errors);
            case PHONE -> validatePhone(question, answer, errors);
            case NUMBER -> validateNumber(question, answer, errors);
            case DATE -> validateDate(question, answer, errors);
            case URL -> validateUrl(question, answer, errors);
        }

        return errors;
    }

    private void validateSingleChoice(Question question, ResponseAnswer answer,
                                      List<ValidationError> errors) {
        // Skip validation if no options are selected and question is not required
        // (Required field validation is handled separately)
        if (answer.optionIds() == null || answer.optionIds().isEmpty()) {
            return;
        }

        // Validate that exactly one option is selected
        if (answer.optionIds().size() > 1) {
            errors.add(new ValidationError(
                    question.questionId(),
                    "Only one option can be selected for this question"
            ));
            return;
        }

        Long selectedOptionId = answer.optionIds().get(0);

        // Validate that the selected option actually belongs to this question
        boolean validOption = question.options().stream()
                .anyMatch(option -> option.optionId().equals(selectedOptionId));

        if (!validOption) {
            errors.add(new ValidationError(
                    question.questionId(),
                    "Invalid option selected for this question"
            ));
            return;
        }

        // If this is a SELECT_ONE_TEXT type question, validate any additional text
        if (question.questionType() == QuestionType.SELECT_ONE_TEXT) {
            QuestionOption selectedOption = question.options().stream()
                    .filter(option -> option.optionId().equals(selectedOptionId))
                    .findFirst()
                    .orElse(null);

            if (selectedOption != null && selectedOption.requiresText()) {
                String additionalText = answer.optionTexts() != null ?
                        answer.optionTexts().get(selectedOptionId) : null;

                // Check if required text is missing
                if (additionalText == null || additionalText.trim().isEmpty()) {
                    errors.add(new ValidationError(
                            question.questionId(),
                            selectedOption.textPrompt() != null ?
                                    selectedOption.textPrompt() :
                                    "Additional text is required for the selected option"
                    ));
                } else if (selectedOption.textValidation() != null) {
                    // Validate the additional text against the option's text validation rules
                    validateTextWithRules(
                            question.questionId(),
                            additionalText,
                            selectedOption.textValidation(),
                            errors
                    );
                }
            } else {
                // If this option doesn't require text but text was provided, add a warning
                if (answer.optionTexts() != null &&
                        answer.optionTexts().containsKey(selectedOptionId) &&
                        !answer.optionTexts().get(selectedOptionId).trim().isEmpty()) {
                    errors.add(new ValidationError(
                            question.questionId(),
                            "Additional text is not accepted for the selected option"
                    ));
                }
            }
        } else {
            // For regular single choice questions, ensure no text was provided
            if (answer.optionTexts() != null && !answer.optionTexts().isEmpty()) {
                errors.add(new ValidationError(
                        question.questionId(),
                        "Text input is not allowed for this question type"
                ));
            }
        }
    }

    private boolean isEmptyAnswer(ResponseAnswer answer) {
        return (answer.optionIds() == null || answer.optionIds().isEmpty()) &&
               (answer.answerText() == null || answer.answerText().isBlank()) &&
               (answer.optionTexts() == null || answer.optionTexts().isEmpty());
    }

    private void validateTextAnswer(Question question, ResponseAnswer answer, 
                                  List<ValidationError> errors) {
        if (answer.answerText() == null) return;

        QuestionValidation validation = question.validation();
        if (validation == null) return;

        String text = answer.answerText();
        
        if (validation.minLength() != null && 
            text.length() < validation.minLength()) {
            errors.add(new ValidationError(
                question.questionId(),
                "Text must be at least " + validation.minLength() + " characters"
            ));
        }

        if (validation.maxLength() != null && 
            text.length() > validation.maxLength()) {
            errors.add(new ValidationError(
                question.questionId(),
                "Text must not exceed " + validation.maxLength() + " characters"
            ));
        }

        if (validation.regex() != null && 
            !Pattern.compile(validation.regex()).matcher(text).matches()) {
            errors.add(new ValidationError(
                question.questionId(),
                validation.errorMessage() != null ? 
                    validation.errorMessage() : "Invalid format"
            ));
        }
    }

    private void validateSelectOneText(Question question, ResponseAnswer answer, 
                                     List<ValidationError> errors) {
        // Validate single selection
        validateSingleChoice(question, answer, errors);
        if (!errors.isEmpty()) return;

        // Validate additional text if required
        if (answer.optionIds() != null && !answer.optionIds().isEmpty()) {
            Long selectedOptionId = answer.optionIds().get(0);
            QuestionOption selectedOption = question.options().stream()
                .filter(opt -> opt.optionId().equals(selectedOptionId))
                .findFirst()
                .orElse(null);

            if (selectedOption != null && selectedOption.requiresText()) {
                String additionalText = answer.optionTexts() != null ? 
                    answer.optionTexts().get(selectedOptionId) : null;

                if (additionalText == null || additionalText.isBlank()) {
                    errors.add(new ValidationError(
                        question.questionId(),
                        "Additional text required for selected option"
                    ));
                } else if (selectedOption.textValidation() != null) {
                    validateTextWithRules(
                        question.questionId(),
                        additionalText,
                        selectedOption.textValidation(),
                        errors
                    );
                }
            }
        }
    }

    private void validateTextWithRules(
            Long questionId,
            String text,
            QuestionValidation validation,
            List<ValidationError> errors
    ) {
        if (text == null || validation == null) return;

        // Trim the text if it's not null
        String trimmedText = text.trim();

        // Minimum length validation
        if (validation.minLength() != null &&
                trimmedText.length() < validation.minLength()) {
            errors.add(new ValidationError(
                    questionId,
                    validation.errorMessage() != null ?
                            validation.errorMessage() :
                            String.format("Text must be at least %d characters", validation.minLength())
            ));
        }

        // Maximum length validation
        if (validation.maxLength() != null &&
                trimmedText.length() > validation.maxLength()) {
            errors.add(new ValidationError(
                    questionId,
                    validation.errorMessage() != null ?
                            validation.errorMessage() :
                            String.format("Text must not exceed %d characters", validation.maxLength())
            ));
        }

        // Regex pattern validation
        if (validation.regex() != null && !trimmedText.isEmpty()) {
            try {
                Pattern pattern = Pattern.compile(validation.regex());
                if (!pattern.matcher(trimmedText).matches()) {
                    errors.add(new ValidationError(
                            questionId,
                            validation.errorMessage() != null ?
                                    validation.errorMessage() :
                                    "Text format is invalid"
                    ));
                }
            } catch (Exception e) {
                // Log invalid regex pattern
                errors.add(new ValidationError(
                        questionId,
                        "Internal validation error: Invalid regex pattern"
                ));
            }
        }

        // Numeric range validation for numeric text
        if ((validation.minValue() != null || validation.maxValue() != null) &&
                !trimmedText.isEmpty()) {
            try {
                double numericValue = Double.parseDouble(trimmedText);

                if (validation.minValue() != null &&
                        numericValue < validation.minValue()) {
                    errors.add(new ValidationError(
                            questionId,
                            validation.errorMessage() != null ?
                                    validation.errorMessage() :
                                    String.format("Value must be at least %.2f", validation.minValue())
                    ));
                }

                if (validation.maxValue() != null &&
                        numericValue > validation.maxValue()) {
                    errors.add(new ValidationError(
                            questionId,
                            validation.errorMessage() != null ?
                                    validation.errorMessage() :
                                    String.format("Value must not exceed %.2f", validation.maxValue())
                    ));
                }
            } catch (NumberFormatException e) {
                // Only add error if numeric validation was actually required
                if (validation.minValue() != null || validation.maxValue() != null) {
                    errors.add(new ValidationError(
                            questionId,
                            validation.errorMessage() != null ?
                                    validation.errorMessage() :
                                    "Value must be a valid number"
                    ));
                }
            }
        }
    }

    // Helper method to validate specific text formats
    private boolean validateFormat(String text, String format) {
        if (text == null || text.isEmpty()) return true;

        return switch (format.toLowerCase()) {
            case "email" -> EMAIL_PATTERN.matcher(text).matches();
            case "phone" -> PHONE_PATTERN.matcher(text).matches();
            case "url" -> URL_PATTERN.matcher(text).matches();
            case "date" -> {
                try {
                    LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
                    yield true;
                } catch (DateTimeParseException e) {
                    yield false;
                }
            }
            default -> true;
        };
    }

    private void validateMultiChoice(Question question, ResponseAnswer answer, 
                                   List<ValidationError> errors) {
        if (answer.optionIds() == null || answer.optionIds().isEmpty()) return;

        // Validate mutual exclusivity
        Set<Long> selectedOptions = new HashSet<>(answer.optionIds());
        for (QuestionOption option : question.options()) {
            if (selectedOptions.contains(option.optionId()) && 
                option.mutuallyExclusive() != null) {
                for (Long mutexId : option.mutuallyExclusive()) {
                    if (selectedOptions.contains(mutexId)) {
                        errors.add(new ValidationError(
                            question.questionId(),
                            "Options '" + option.optionText() + "' and '" +
                            question.options().stream()
                                .filter(o -> o.optionId().equals(mutexId))
                                .findFirst()
                                .map(QuestionOption::optionText)
                                .orElse("Unknown") +
                            "' cannot be selected together"
                        ));
                    }
                }
            }
        }

        // Validate dependencies
        for (QuestionOption option : question.options()) {
            if (selectedOptions.contains(option.optionId()) && 
                option.dependencies() != null) {
                for (Long depId : option.dependencies()) {
                    if (!selectedOptions.contains(depId)) {
                        errors.add(new ValidationError(
                            question.questionId(),
                            "Option '" + option.optionText() + "' requires '" +
                            question.options().stream()
                                .filter(o -> o.optionId().equals(depId))
                                .findFirst()
                                .map(QuestionOption::optionText)
                                .orElse("Unknown") +
                            "' to be selected"
                        ));
                    }
                }
            }
        }
    }

    private void validateEmail(Question question, ResponseAnswer answer, 
                             List<ValidationError> errors) {
        if (answer.answerText() != null && 
            !EMAIL_PATTERN.matcher(answer.answerText()).matches()) {
            errors.add(new ValidationError(
                question.questionId(),
                "Invalid email format"
            ));
        }
    }

    private void validatePhone(Question question, ResponseAnswer answer, 
                             List<ValidationError> errors) {
        if (answer.answerText() != null && 
            !PHONE_PATTERN.matcher(answer.answerText()).matches()) {
            errors.add(new ValidationError(
                question.questionId(),
                "Invalid phone number format"
            ));
        }
    }

    private void validateNumber(Question question, ResponseAnswer answer, 
                              List<ValidationError> errors) {
        if (answer.answerText() == null) return;

        try {
            double value = Double.parseDouble(answer.answerText());
            QuestionValidation validation = question.validation();
            
            if (validation != null) {
                if (validation.minValue() != null && 
                    value < validation.minValue()) {
                    errors.add(new ValidationError(
                        question.questionId(),
                        "Value must be at least " + validation.minValue()
                    ));
                }
                if (validation.maxValue() != null && 
                    value > validation.maxValue()) {
                    errors.add(new ValidationError(
                        question.questionId(),
                        "Value must not exceed " + validation.maxValue()
                    ));
                }
            }
        } catch (NumberFormatException e) {
            errors.add(new ValidationError(
                question.questionId(),
                "Invalid number format"
            ));
        }
    }

    private void validateDate(Question question, ResponseAnswer answer, 
                            List<ValidationError> errors) {
        if (answer.answerText() == null) return;

        try {
            LocalDate.parse(answer.answerText(), 
                          DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            errors.add(new ValidationError(
                question.questionId(),
                "Invalid date format. Use YYYY-MM-DD"
            ));
        }
    }

    private void validateUrl(Question question, ResponseAnswer answer, 
                           List<ValidationError> errors) {
        if (answer.answerText() != null && 
            !URL_PATTERN.matcher(answer.answerText()).matches()) {
            errors.add(new ValidationError(
                question.questionId(),
                "Invalid URL format"
            ));
        }
    }
}