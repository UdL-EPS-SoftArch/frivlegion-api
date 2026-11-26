package com.frivlegion.platform.handler;

import com.frivlegion.platform.domain.GameState;
import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.repository.GameStateRepository;
import com.frivlegion.platform.security.CurrentPlayer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class GameStateEventHandler {

    private final GameStateRepository gameStateRepository;
    private final CurrentPlayer currentPlayer;

    public GameStateEventHandler(GameStateRepository gameStateRepository, CurrentPlayer currentPlayer) {
        this.gameStateRepository = gameStateRepository;
        this.currentPlayer = currentPlayer;
    }

    @HandleBeforeCreate
    public void handleGameStatePreCreate(GameState gameState) {
        Player player = currentPlayer.require();
        gameState.setPlayer(player);
        if (gameState.getGame() != null
            && gameStateRepository.existsByPlayerIdAndGameId(player.getId(), gameState.getGame().getId())) {
            throw new DataIntegrityViolationException(
                "A game state already exists for this player and game; update it instead");
        }
    }

    @HandleBeforeSave
    public void handleGameStatePreSave(GameState gameState) {
        Player owner = persistedOwner(gameState);
        currentPlayer.requireOwner(owner);
        gameState.setPlayer(owner);
    }

    @HandleBeforeDelete
    public void handleGameStatePreDelete(GameState gameState) {
        currentPlayer.requireOwner(persistedOwner(gameState));
    }

    /** The owner stored in the database, never the one sent by the client. */
    private Player persistedOwner(GameState gameState) {
        if (gameState.getId() == null) {
            return gameState.getPlayer();
        }
        return gameStateRepository.findOwnerById(gameState.getId()).orElse(null);
    }
}
