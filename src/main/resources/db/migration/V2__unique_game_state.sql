-- Issue #28: one game state per player and game.
-- Keep only the most recent row (highest id) for any duplicated (player_id, game_id) pair.
DELETE FROM game_state gs
WHERE EXISTS (
    SELECT 1 FROM game_state newer
    WHERE newer.player_id = gs.player_id
      AND newer.game_id = gs.game_id
      AND newer.id > gs.id
);

ALTER TABLE game_state
    ADD CONSTRAINT uq_game_state_player_game UNIQUE (player_id, game_id);
