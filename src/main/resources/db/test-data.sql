INSERT INTO users (username, email, password, role, created_at, updated_at) 
VALUES ('testuser', 'test@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'USER', NOW(), NOW())
ON CONFLICT (username) DO NOTHING;

INSERT INTO quests (title, description, author_id, created_at, updated_at)
VALUES ('Тестовый квест', 'Простой квест для тестирования', 1, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO quest_stages (quest_id, stage_id, title, description, result_type, stage_index, created_at, updated_at)
VALUES 
    (1, 'test_start', 'Начало теста', 'Вы находитесь в начале тестового квеста', 'NONE', 1, NOW(), NOW()),
    (1, 'test_win', 'Победа', 'Поздравляем! Вы прошли тест!', 'WIN', 2, NOW(), NOW()),
    (1, 'test_lose', 'Поражение', 'К сожалению, тест не пройден', 'LOSE', 3, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO quest_stage_options (stage_id, option_key, option_text, target_stage_id, created_at, updated_at)
VALUES 
    (1, 'win', 'Выбрать победу', 'test_win', NOW(), NOW()),
    (1, 'lose', 'Выбрать поражение', 'test_lose', NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO user_quest_progress (user_id, quest_id, current_stage_id, is_completed, started_at, created_at, updated_at)
VALUES (1, 1, 'test_start', false, NOW(), NOW(), NOW())
ON CONFLICT DO NOTHING;

SELECT 
    'Пользователи' as table_name, 
    COUNT(*) as count 
FROM users
UNION ALL
SELECT 
    'Квесты' as table_name, 
    COUNT(*) as count 
FROM quests
UNION ALL
SELECT 
    'Этапы квестов' as table_name, 
    COUNT(*) as count 
FROM quest_stages
UNION ALL
SELECT 
    'Опции этапов' as table_name, 
    COUNT(*) as count 
FROM quest_stage_options
UNION ALL
SELECT 
    'Прогресс игр' as table_name, 
    COUNT(*) as count 
FROM user_quest_progress;
