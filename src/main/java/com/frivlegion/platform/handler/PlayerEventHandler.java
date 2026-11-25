package com.frivlegion.platform.handler;

import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.repository.GameStateRepository;
import com.frivlegion.platform.repository.RecordRepository;
import com.frivlegion.platform.repository.ScoreRepository;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RepositoryEventHandler
public class PlayerEventHandler {

    private final ScoreRepository scoreRepository;
    private final GameStateRepository gameStateRepository;
    private final RecordRepository recordRepository;

    public PlayerEventHandler(ScoreRepository scoreRepository,
                              GameStateRepository gameStateRepository,
                              RecordRepository recordRepository) {
        this.scoreRepository = scoreRepository;
        this.gameStateRepository = gameStateRepository;
        this.recordRepository = recordRepository;
    }

    /** Registration only goes through POST /auth/register, which validates and issues a JWT (#40). */
    @HandleBeforeCreate
    public void handlePlayerPreCreate(Player player) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
            "Players are created through POST /auth/register");
    }

    /** Profile self-service (#29): only the player themselves or an admin. */
    @HandleBeforeSave
    public void handlePlayerPreSave(Player player) {
        requireSelfOrAdmin(player);
        player.encodePassword();
    }

    /** Account deletion (#29): only the player or an admin; their data goes with them. */
    @HandleBeforeDelete
    public void handlePlayerPreDelete(Player player) {
        requireSelfOrAdmin(player);
        scoreRepository.deleteAll(scoreRepository.findByPlayerId(player.getId()));
        gameStateRepository.deleteAll(gameStateRepository.findByPlayerId(player.getId()));
        recordRepository.deleteAll(recordRepository.findByOwnedById(player.getId()));
    }

    @HandleAfterSave
    public void clearRawPassword(Player player) {
        player.setRawPassword(null);
    }

    private void requireSelfOrAdmin(Player player) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Player current)) {
            throw new AccessDeniedException("Authentication required");
        }
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!isAdmin && !current.getId().equals(player.getId())) {
            throw new AccessDeniedException("Players can only modify their own profile");
        }
    }
}
