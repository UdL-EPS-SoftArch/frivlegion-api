-- Issue #11: the URL the platform loads in the iframe.
ALTER TABLE game ADD COLUMN game_url VARCHAR(255);

-- Issue #29: versioned seed of the initial catalog (non-test environments run Flyway).
-- Minigames are served by the frontend under /minigames/<id>/.
INSERT INTO game (id, title, description, thumbnail_url, game_url, category, is_active, version) VALUES
    ('snake', 'Snake', 'Guide the snake, eat the apples and do not bite your own tail.',
     '/minigames/snake/thumbnail.png', '/minigames/snake/index.html', 'Arcade', TRUE, 0),
    ('memory-match', 'Memory Match', 'Flip the cards and find every pair in as few moves as possible.',
     '/minigames/memory-match/thumbnail.png', '/minigames/memory-match/index.html', 'Puzzle', TRUE, 0),
    ('tic-tac-toe', 'Tic Tac Toe', 'Line up three marks before the computer does.',
     '/minigames/tic-tac-toe/thumbnail.png', '/minigames/tic-tac-toe/index.html', 'Strategy', TRUE, 0);
