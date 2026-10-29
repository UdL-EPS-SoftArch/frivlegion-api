package com.frivlegion.platform.handler;

import com.frivlegion.platform.domain.GameState;
import com.frivlegion.platform.domain.Player;
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

    @HandleBeforeCreate
    public void handleGameStatePreCreate(GameState gameState) {
        gameState.setPlayer(currentPlayer());
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
