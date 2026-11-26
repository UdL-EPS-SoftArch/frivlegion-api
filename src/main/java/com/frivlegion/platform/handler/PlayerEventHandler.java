package com.frivlegion.platform.handler;

import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.repository.GameStateRepository;
import com.frivlegion.platform.repository.RecordRepository;
import com.frivlegion.platform.repository.ScoreRepository;
import com.frivlegion.platform.security.CurrentPlayer;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RepositoryEventHandler
public class PlayerEventHandler {

    private final ScoreRepository scoreRepository;
    private final GameStateRepository gameStateRepository;
    private final RecordRepository recordRepository;
    private final CurrentPlayer currentPlayer;

    public PlayerEventHandler(ScoreRepository scoreRepository,
                              GameStateRepository gameStateRepository,
                              RecordRepository recordRepository,
                              CurrentPlayer currentPlayer) {
        this.scoreRepository = scoreRepository;
        this.gameStateRepository = gameStateRepository;
        this.recordRepository = recordRepository;
        this.currentPlayer = currentPlayer;
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
        currentPlayer.requireSelfOrAdmin(player);
        player.encodePassword();
    }

    /** Account deletion (#29): only the player or an admin; their data goes with them. */
    @HandleBeforeDelete
    public void handlePlayerPreDelete(Player player) {
        currentPlayer.requireSelfOrAdmin(player);
        scoreRepository.deleteAll(scoreRepository.findByPlayerId(player.getId()));
        gameStateRepository.deleteAll(gameStateRepository.findByPlayerId(player.getId()));
        recordRepository.deleteAll(recordRepository.findByOwnedById(player.getId()));
    }

    @HandleAfterSave
    public void clearRawPassword(Player player) {
        player.setRawPassword(null);
    }
}
