package com.frivlegion.platform.handler;

import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.domain.Score;
import com.frivlegion.platform.repository.ScoreRepository;
import com.frivlegion.platform.security.CurrentPlayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScoreEventHandlerTest {

    private final ScoreRepository scoreRepository = mock(ScoreRepository.class);
    private final ScoreEventHandler handler = new ScoreEventHandler(scoreRepository, new CurrentPlayer());

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ownerCanDeleteOwnScore() {
        Player owner = player(1L, "owner", false);
        Score score = new Score();
        score.setId(7L);
        score.setPlayer(owner);
        when(scoreRepository.findById(7L)).thenReturn(Optional.of(score));
        authenticate(owner);

        assertDoesNotThrow(() -> handler.handleScorePreDelete(score));
    }

    @Test
    void otherPlayerCannotDeleteScore() {
        Player owner = player(1L, "owner", false);
        Player intruder = player(2L, "intruder", false);
        Score score = new Score();
        score.setId(7L);
        score.setPlayer(owner);
        when(scoreRepository.findById(7L)).thenReturn(Optional.of(score));
        authenticate(intruder);

        assertThrows(AccessDeniedException.class, () -> handler.handleScorePreDelete(score));
    }

    @Test
    void adminCanDeleteAnyScore() {
        Player owner = player(1L, "owner", false);
        Player admin = player(2L, "admin", true);
        Score score = new Score();
        score.setId(7L);
        score.setPlayer(owner);
        when(scoreRepository.findById(7L)).thenReturn(Optional.of(score));
        authenticate(admin);

        assertDoesNotThrow(() -> handler.handleScorePreDelete(score));
    }

    private static Player player(Long id, String username, boolean admin) {
        Player player = new Player();
        player.setId(id);
        player.setUsername(username);
        player.setAdmin(admin);
        return player;
    }

    private static void authenticate(Player player) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(player, "test", player.getAuthorities()));
    }
}
