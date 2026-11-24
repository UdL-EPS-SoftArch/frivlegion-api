-- Issue #17 / #29: administrators (ROLE_ADMIN) manage the game catalog.
ALTER TABLE player ADD COLUMN admin BOOLEAN NOT NULL DEFAULT FALSE;
