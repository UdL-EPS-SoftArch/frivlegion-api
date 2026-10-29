package com.frivlegion.platform.config;
import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.domain.Record;
import com.frivlegion.platform.repository.PlayerRepository;
import com.frivlegion.platform.repository.RecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

import java.util.Arrays;

@Configuration
public class DBInitialization {
    @Value("${default-password}")
    String defaultPassword;

    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    private final RecordRepository recordRepository;
    private final PlayerRepository playerRepository;

    public DBInitialization(PlayerRepository playerRepository, RecordRepository recordRepository) {
        this.playerRepository = playerRepository;
        this.recordRepository = recordRepository;
    }

    @PostConstruct
    public void initializeDatabase() {
        // Default user
        if (!playerRepository.existsByUsername("player")) {
            Player player = new Player();
            player.setUsername("player");
            player.setEmail("player@sample.app");
            player.setRawPassword(defaultPassword);
            player.encodePassword();
            playerRepository.save(player);
        }
        if (Arrays.asList(activeProfiles.split(",")).contains("test")) {
            // Testing instances
            if (!playerRepository.existsByUsername("test")) {
                Player player = new Player();
                player.setUsername("test");
                player.setEmail("test@sample.app");
                player.setRawPassword(defaultPassword);
                player.encodePassword();
                player = playerRepository.save(player);
                Record record = new Record();
                record.setName("My test record");
                record.setDescription("A record used for testing purposes, nothing more, nothing less...");
                record.setOwnedBy(player);
                recordRepository.save(record);
            }
        }
    }
}
