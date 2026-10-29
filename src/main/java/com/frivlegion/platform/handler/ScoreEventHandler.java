package com.frivlegion.platform.handler;

import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.domain.Score;
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

    @HandleBeforeCreate
    public void handleScorePreCreate(Score score) {
        score.setPlayer(currentPlayer());
    }

    @HandleBeforeSave
    public void handleScorePreSave(Score score) {
        requireOwner(score);
    }

    @HandleBeforeDelete
    public void handleScorePreDelete(Score score) {
        requireOwner(score);
    }

    private void requireOwner(Score score) {
        if (score.getPlayer() == null
            || !currentPlayer().getUsername().equals(score.getPlayer().getUsername())) {
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
