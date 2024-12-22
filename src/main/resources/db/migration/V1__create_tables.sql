-- Create tables for survey system
CREATE TABLE surveys (
                         survey_id SERIAL PRIMARY KEY,
                         title VARCHAR(255) NOT NULL,
                         description TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         is_active BOOLEAN DEFAULT true,
                         start_date TIMESTAMP,
                         end_date TIMESTAMP
);

CREATE TABLE questions (
                           question_id SERIAL PRIMARY KEY,
                           survey_id INTEGER REFERENCES surveys(survey_id),
                           question_text TEXT NOT NULL,
                           question_type VARCHAR(50) NOT NULL,
                           is_required BOOLEAN DEFAULT true,
                           display_order INTEGER NOT NULL,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE question_options (
                                  option_id SERIAL PRIMARY KEY,
                                  question_id INTEGER REFERENCES questions(question_id),
                                  option_text TEXT NOT NULL,
                                  display_order INTEGER NOT NULL,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE branching_rules (
                                 rule_id SERIAL PRIMARY KEY,
                                 question_id INTEGER REFERENCES questions(question_id),
                                 option_id INTEGER REFERENCES question_options(option_id),
                                 next_question_id INTEGER REFERENCES questions(question_id),
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE respondents (
                             respondent_id SERIAL PRIMARY KEY,
                             email VARCHAR(255) UNIQUE,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE responses (
                           response_id SERIAL PRIMARY KEY,
                           survey_id INTEGER REFERENCES surveys(survey_id),
                           respondent_id INTEGER REFERENCES respondents(respondent_id),
                           submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE response_answers (
                                  answer_id SERIAL PRIMARY KEY,
                                  response_id INTEGER REFERENCES responses(response_id),
                                  question_id INTEGER REFERENCES questions(question_id),
                                  option_id INTEGER REFERENCES question_options(option_id),
                                  answer_text TEXT,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_questions_survey_id ON questions(survey_id);
CREATE INDEX idx_question_options_question_id ON question_options(question_id);
CREATE INDEX idx_branching_rules_question_id ON branching_rules(question_id);
CREATE INDEX idx_responses_survey_id ON responses(survey_id);
CREATE INDEX idx_response_answers_response_id ON response_answers(response_id);