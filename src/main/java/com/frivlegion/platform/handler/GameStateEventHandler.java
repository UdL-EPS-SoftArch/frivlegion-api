package com.frivlegion.platform.handler;

import com.frivlegion.platform.domain.GameState;
import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.repository.GameStateRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class GameStateEventHandler {

    private final GameStateRepository gameStateRepository;

    public GameStateEventHandler(GameStateRepository gameStateRepository) {
        this.gameStateRepository = gameStateRepository;
    }

    @HandleBeforeCreate
    public void handleGameStatePreCreate(GameState gameState) {
        Player player = currentPlayer();
        gameState.setPlayer(player);
        if (gameState.getGame() != null
            && gameStateRepository.existsByPlayerIdAndGameId(player.getId(), gameState.getGame().getId())) {
            throw new DataIntegrityViolationException(
                "A game state already exists for this player and game; update it instead");
        }
    }

    @HandleBeforeSave
    public void handleGameStatePreSave(GameState gameState) {
        requireOwner(gameState);
    }

    @HandleBeforeDelete
    public void handleGameStatePreDelete(GameState gameState) {
        requireOwner(gameState);
    }

    private void requireOwner(GameState gameState) {
        if (gameState.getPlayer() == null
            || !currentPlayer().getUsername().equals(gameState.getPlayer().getUsername())) {
            throw new AccessDeniedException("Only the owner can modify the game state");
        }
    }

    private Player currentPlayer() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Player)) {
            throw new AccessDeniedException("Authentication required");
        }
        return (Player) principal;
    }
}
