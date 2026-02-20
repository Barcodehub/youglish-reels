-- YouGlish Reels - Database Initialization Script
-- Execute this script to create the database and set up initial configuration

-- Create database (execute separately)
-- CREATE DATABASE youglish_reels;

-- Connect to the database
\c youglish_reels;

-- Create tables (will be created automatically by Hibernate, but here's the schema)

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Phrases table
CREATE TABLE IF NOT EXISTS phrases (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    text VARCHAR(200) NOT NULL,
    language VARCHAR(50) NOT NULL DEFAULT 'english',
    accent VARCHAR(50),
    total_videos_available INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP
);

-- Video History table
CREATE TABLE IF NOT EXISTS video_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    phrase_id BIGINT NOT NULL REFERENCES phrases(id) ON DELETE CASCADE,
    video_id VARCHAR(20) NOT NULL,
    track_number INTEGER NOT NULL,
    caption_text TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_phrases_user_active ON phrases(user_id, is_active);
CREATE INDEX IF NOT EXISTS idx_video_history_user_created ON video_history(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_video_history_user_video_phrase ON video_history(user_id, video_id, phrase_id);

-- Optional: Insert demo data for testing
-- Demo user: username=demo, password=demo123 (will be encrypted by backend)
-- INSERT INTO users (username, email, password, enabled)
-- VALUES ('demo', 'demo@example.com', '$2a$10$... (BCrypt hash)', TRUE);

-- Demo phrases for demo user (user_id = 1)
-- INSERT INTO phrases (user_id, text, language, total_videos_available, is_active)
-- VALUES
--     (1, 'great power', 'english', 250, TRUE),
--     (1, 'common sense', 'english', 180, TRUE),
--     (1, 'time management', 'english', 120, TRUE);

-- Grant permissions (adjust as needed)
-- GRANT ALL PRIVILEGES ON DATABASE youglish_reels TO postgres;
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;

-- View table structure
-- \dt
-- \d users
-- \d phrases
-- \d video_history

-- Useful queries for monitoring

-- Count users
-- SELECT COUNT(*) FROM users;

-- Count active phrases per user
-- SELECT u.username, COUNT(p.id) as active_phrases
-- FROM users u
-- LEFT JOIN phrases p ON u.id = p.user_id AND p.is_active = TRUE
-- GROUP BY u.username;

-- Recent video history
-- SELECT vh.created_at, u.username, p.text, vh.video_id, vh.track_number
-- FROM video_history vh
-- JOIN users u ON vh.user_id = u.id
-- JOIN phrases p ON vh.phrase_id = p.id
-- ORDER BY vh.created_at DESC
-- LIMIT 20;

-- Clean old history (keeps last 100 per user)
-- WITH ranked_history AS (
--     SELECT id, ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY created_at DESC) as rn
--     FROM video_history
-- )
-- DELETE FROM video_history WHERE id IN (
--     SELECT id FROM ranked_history WHERE rn > 100
-- );

