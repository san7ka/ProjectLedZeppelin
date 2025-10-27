-- Test Data for Hibernate Tests

-- Insert test users
INSERT INTO users (id, username, email, password, role, created_at, updated_at) VALUES
(1, 'testuser', 'test@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVhHOnS4Qb/8qJdKzqKzqKzqKz', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'testauthor', 'author@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVhHOnS4Qb/8qJdKzqKzqKzqKz', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVhHOnS4Qb/8qJdKzqKzqKzqKz', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test quests
INSERT INTO quests (id, title, description, author_id, created_at, updated_at) VALUES
(1, 'Test Quest', 'A test quest for unit testing', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Another Quest', 'Another test quest', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test quest stages
INSERT INTO quest_stages (id, stage_id, title, description, quest_id, stage_index, result_type, created_at, updated_at) VALUES
(1, 'start', 'Start Stage', 'You are at the beginning', 1, 1, 'NONE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'win', 'Win Stage', 'You win!', 1, 2, 'WIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'lose', 'Lose Stage', 'You lose!', 1, 3, 'LOSE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'start2', 'Start Stage 2', 'Beginning of second quest', 2, 1, 'NONE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test quest stage options
INSERT INTO quest_stage_options (id, stage_id, option_key, option_text, target_stage_id, created_at, updated_at) VALUES
(1, 1, 'win_choice', 'Choose to win', 'win', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'lose_choice', 'Choose to lose', 'lose', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 4, 'continue', 'Continue', 'start2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test user quest progress
INSERT INTO user_quest_progress (id, user_id, quest_id, current_stage_id, is_completed, started_at, completed_at, created_at, updated_at) VALUES
(1, 1, 1, 'start', false, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 2, 'start2', false, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Reset sequences
ALTER SEQUENCE users_seq RESTART WITH 4;
ALTER SEQUENCE quests_seq RESTART WITH 3;
ALTER SEQUENCE quest_stages_seq RESTART WITH 5;
ALTER SEQUENCE quest_stage_options_seq RESTART WITH 4;
ALTER SEQUENCE user_quest_progress_seq RESTART WITH 3;
