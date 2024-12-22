-- Add validation table
CREATE TABLE condition_summary(
          session_id SERIAL PRIMARY KEY,
          summary TEXT,
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

