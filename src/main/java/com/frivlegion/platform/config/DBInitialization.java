package com.frivlegion.platform.config;
import com.frivlegion.platform.domain.Record;
import com.frivlegion.platform.domain.User;
import com.frivlegion.platform.repository.RecordRepository;
import com.frivlegion.platform.repository.UserRepository;
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
    private final UserRepository userRepository;

    public DBInitialization(UserRepository userRepository, RecordRepository recordRepository) {
        this.userRepository = userRepository;
        this.recordRepository = recordRepository;
    }

    @PostConstruct
    public void initializeDatabase() {
        // Default user
        if (!userRepository.existsById("player")) {
            User user = new User();
            user.setEmail("player@sample.app");
            user.setId("player");
            user.setPassword(defaultPassword);
            user.encodePassword();
            userRepository.save(user);
        }
        if (Arrays.asList(activeProfiles.split(",")).contains("test")) {
            // Testing instances
            if (!userRepository.existsById("test")) {
                User user = new User();
                user.setEmail("test@sample.app");
                user.setId("test");
                user.setPassword(defaultPassword);
                user.encodePassword();
                user = userRepository.save(user);
                com.frivlegion.platform.domain.Record record = new Record();
                record.setName("My test record");
                record.setDescription("A record used for testing purposes, nothing more, nothing less...");
                record.setOwnedBy(user);
                recordRepository.save(record);
            }
        }
    }
}
