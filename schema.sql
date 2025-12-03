-- PostgreSQL Schema Configuration for WebGuide Backend with PGRoonga
-- This file defines the necessary tables and PGRoonga indexes for the search functionality.

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS pgroonga;

-- Table: webguide.notice
-- This table is assumed to exist based on the application's repository queries.
-- We are adding PGRoonga indexes to its columns for full-text search.
-- The table structure is inferred from NoticeSearchRepository.java

-- Example table creation if it doesn't exist (commented out, as assumed to exist)
-- CREATE TABLE IF NOT EXISTS webguide.notice (
--     notice_id BIGSERIAL PRIMARY KEY,
--     category VARCHAR(255),
--     title VARCHAR(500) NOT NULL,
--     summary TEXT,
--     content TEXT,
--     views INT DEFAULT 0,
--     use_yn CHAR(1) DEFAULT 'Y',
--     is_new BOOLEAN DEFAULT FALSE,
--     reg_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     mod_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     delete_yn CHAR(1) DEFAULT 'N',
--     portal_id VARCHAR(255)
-- );

-- PGRoonga index for webguide.notice table for title, summary, content
CREATE INDEX IF NOT EXISTS pgroonga_notice_search_index ON webguide.notice USING pgroonga (title, summary, content);

-- Table: webguide.guides
-- This table is assumed to exist based on the application's repository queries.
-- We are adding PGRoonga indexes to its columns for full-text search.
-- The table structure is inferred from GuideVersionRepository.java

-- Example table creation if it doesn't exist (commented out, as assumed to exist)
-- CREATE TABLE IF NOT EXISTS webguide.guides (
--     id BIGSERIAL PRIMARY KEY,
--     category_id BIGINT,
--     portal_id VARCHAR(255),
--     delete_yn BOOLEAN DEFAULT FALSE,
--     current_version_id INT
-- );

-- Table: webguide.guide_versions
-- This table is assumed to exist based on the application's repository queries.
-- We are adding PGRoonga indexes to its columns for full-text search.
-- The table structure is inferred from GuideVersionRepository.java

-- Example table creation if it doesn't exist (commented out, as assumed to exist)
-- CREATE TABLE IF NOT EXISTS webguide.guide_versions (
--     id BIGSERIAL PRIMARY KEY,
--     guide_id BIGINT NOT NULL,
--     version INT NOT NULL,
--     content_body TEXT,
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     FOREIGN KEY (guide_id) REFERENCES webguide.guides(id)
-- );

-- PGRoonga index for webguide.guide_versions table for content_body
CREATE INDEX IF NOT EXISTS pgroonga_guide_versions_content_search_index ON webguide.guide_versions USING pgroonga (content_body);
