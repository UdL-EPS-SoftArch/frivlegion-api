package com.frivlegion.platform.handler;

import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.domain.Score;
import com.frivlegion.platform.repository.ScoreRepository;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class ScoreEventHandler {

    private final ScoreRepository scoreRepository;

    public ScoreEventHandler(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    @HandleBeforeCreate
    public void handleScorePreCreate(Score score) {
        score.setPlayer(currentPlayer());
    }

    @HandleBeforeSave
    public void handleScorePreSave(Score score) {
        Player owner = persistedOwner(score);
        requireOwner(owner);
        score.setPlayer(owner);
    }

    @HandleBeforeDelete
    public void handleScorePreDelete(Score score) {
        requireOwner(persistedOwner(score));
    }

    private Player persistedOwner(Score score) {
        if (score.getId() == null) {
            return score.getPlayer();
        }
        return scoreRepository.findById(score.getId())
            .map(Score::getPlayer)
            .orElse(null);
    }

    private void requireOwner(Player owner) {
        if (owner == null
            || !currentPlayer().getUsername().equals(owner.getUsername())) {
            throw new AccessDeniedException("Only the owner can modify the score");
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
