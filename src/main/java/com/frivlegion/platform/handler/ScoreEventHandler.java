package com.frivlegion.platform.handler;

import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.domain.Score;
import com.frivlegion.platform.repository.ScoreRepository;
import com.frivlegion.platform.security.CurrentPlayer;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class ScoreEventHandler {

    private final ScoreRepository scoreRepository;
    private final CurrentPlayer currentPlayer;

    public ScoreEventHandler(ScoreRepository scoreRepository, CurrentPlayer currentPlayer) {
        this.scoreRepository = scoreRepository;
        this.currentPlayer = currentPlayer;
    }

    @HandleBeforeCreate
    public void handleScorePreCreate(Score score) {
        score.setPlayer(currentPlayer.require());
    }

    @HandleBeforeSave
    public void handleScorePreSave(Score score) {
        Player owner = persistedOwner(score);
        currentPlayer.requireOwner(owner);
        score.setPlayer(owner);
    }

    @HandleBeforeDelete
    public void handleScorePreDelete(Score score) {
        currentPlayer.requireOwner(persistedOwner(score));
    }

    private Player persistedOwner(Score score) {
        if (score.getId() == null) {
            return score.getPlayer();
        }
        return scoreRepository.findById(score.getId())
            .map(Score::getPlayer)
            .orElse(null);
    }
}
