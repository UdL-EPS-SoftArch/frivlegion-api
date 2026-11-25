package com.frivlegion.platform.config;

import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Creates (or promotes) an administrator in any environment when ADMIN_USERNAME,
 * ADMIN_EMAIL and ADMIN_PASSWORD are set, e.g. as Fly.io secrets (#29).
 * Nothing happens when they are empty.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final PlayerRepository playerRepository;
    private final String username;
    private final String email;
    private final String password;

    public AdminBootstrap(PlayerRepository playerRepository,
                          @Value("${frivlegion.admin.username:}") String username,
                          @Value("${frivlegion.admin.email:}") String email,
                          @Value("${frivlegion.admin.password:}") String password) {
        this.playerRepository = playerRepository;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(username)) {
            return;
        }
        Player admin = playerRepository.findByUsername(username).orElse(null);
        if (admin == null) {
            if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
                log.warn("ADMIN_USERNAME is set but ADMIN_EMAIL or ADMIN_PASSWORD is missing; no admin created");
                return;
            }
            admin = new Player();
            admin.setUsername(username);
            admin.setEmail(email);
            admin.setRawPassword(password);
            admin.encodePassword();
            admin.setRawPassword(null);
        }
        if (!admin.isAdmin()) {
            admin.setAdmin(true);
            playerRepository.save(admin);
            log.info("Administrator '{}' is ready", username);
        }
    }
}
