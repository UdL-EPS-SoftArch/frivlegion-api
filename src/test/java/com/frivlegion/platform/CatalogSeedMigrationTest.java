package com.frivlegion.platform;

import com.frivlegion.platform.domain.Game;
import com.frivlegion.platform.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Default profile = Flyway enabled: the versioned catalog seed (V4) must be applied (#29). */
@SpringBootTest
class CatalogSeedMigrationTest {

    @Autowired
    private GameRepository gameRepository;

    @Test
    void flywaySeedsTheInitialCatalog() {
        Game snake = gameRepository.findById("snake").orElseThrow();
        assertEquals("/minigames/snake/index.html", snake.getGameUrl());
        assertTrue(gameRepository.findByIsActiveTrue().size() >= 3);
    }
}
