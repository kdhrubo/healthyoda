-- Add validation table
CREATE TABLE question_validations (
                                      validation_id SERIAL PRIMARY KEY,
                                      question_id INTEGER REFERENCES questions(question_id),
                                      min_length INTEGER,
                                      max_length INTEGER,
                                      regex TEXT,
                                      min_value DOUBLE PRECISION,
                                      max_value DOUBLE PRECISION,
                                      error_message TEXT,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add option dependencies table
CREATE TABLE option_dependencies (
                                     dependency_id SERIAL PRIMARY KEY,
                                     option_id INTEGER REFERENCES question_options(option_id),
                                     dependent_option_id INTEGER REFERENCES question_options(option_id),
                                     is_exclusive BOOLEAN DEFAULT false, -- true for mutually exclusive, false for required
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add option text requirements
ALTER TABLE question_options
    ADD COLUMN requires_text BOOLEAN DEFAULT false,
ADD COLUMN text_prompt TEXT,
ADD COLUMN validation_id INTEGER REFERENCES question_validations(validation_id);

-- Add option text responses
CREATE TABLE option_text_responses (
                                       response_id INTEGER REFERENCES responses(response_id),
                                       question_id INTEGER REFERENCES questions(question_id),
                                       option_id INTEGER REFERENCES question_options(option_id),
                                       text_value TEXT,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       PRIMARY KEY (response_id, question_id, option_id)
);

-- Add indices for performance
CREATE INDEX idx_question_validations_question_id
    ON question_validations(question_id);
CREATE INDEX idx_option_dependencies_option_id
    ON option_dependencies(option_id);
CREATE INDEX idx_option_dependencies_dependent_option_id
    ON option_dependencies(dependent_option_id);
CREATE INDEX idx_option_text_responses_response_question
    ON option_text_responses(response_id, question_id);