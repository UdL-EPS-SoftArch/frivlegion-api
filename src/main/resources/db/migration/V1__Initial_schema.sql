-- V1__Initial_schema.sql
-- Initial database schema for FrivLegion
-- Dialect: PostgreSQL (compatible with Neon)

CREATE TABLE player (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    uri VARCHAR(255),
    version BIGINT,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uq_player_username UNIQUE (username),
    CONSTRAINT uq_player_email UNIQUE (email)
);

CREATE TABLE game (
    id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    thumbnail_url VARCHAR(255),
    category VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT
);

CREATE TABLE game_state (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    game_id VARCHAR(255) NOT NULL,
    payload TEXT,
    updated_at TIMESTAMP(6),
    version BIGINT,
    CONSTRAINT fk_game_state_player FOREIGN KEY (player_id) REFERENCES player (id),
    CONSTRAINT fk_game_state_game FOREIGN KEY (game_id) REFERENCES game (id)
);

CREATE TABLE record (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(255) NOT NULL DEFAULT 'PRIVATE',
    created TIMESTAMP(6),
    modified TIMESTAMP(6),
    owned_by_id BIGINT,
    version BIGINT,
    CONSTRAINT fk_record_player FOREIGN KEY (owned_by_id) REFERENCES player (id)
);

CREATE TABLE score (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    game_id VARCHAR(255) NOT NULL,
    score_value INTEGER NOT NULL,
    achieved_at TIMESTAMP(6),
    version BIGINT,
    CONSTRAINT fk_score_player FOREIGN KEY (player_id) REFERENCES player (id),
    CONSTRAINT fk_score_game FOREIGN KEY (game_id) REFERENCES game (id)
);
