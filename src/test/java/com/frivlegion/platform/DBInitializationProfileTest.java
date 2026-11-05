package com.frivlegion.platform;

import com.frivlegion.platform.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("flyio")
class DBInitializationProfileTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void doesNotSeedDefaultPlayerInProductionProfile() {
        assertFalse(playerRepository.existsByUsername("player"));
    }
}
