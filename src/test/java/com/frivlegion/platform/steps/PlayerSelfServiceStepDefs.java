package com.frivlegion.platform.steps;

import com.frivlegion.platform.domain.Game;
import com.frivlegion.platform.domain.GameState;
import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.domain.Score;
import com.frivlegion.platform.repository.GameRepository;
import com.frivlegion.platform.repository.GameStateRepository;
import com.frivlegion.platform.repository.PlayerRepository;
import com.frivlegion.platform.repository.ScoreRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class PlayerSelfServiceStepDefs {

    private static final String PASSWORD = "password123";
    private static final String ADMIN = "admin";
    private static final String ADMIN_PASSWORD = "password";

    private final StepDefs stepDefs;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final ScoreRepository scoreRepository;
    private final GameStateRepository gameStateRepository;

    private Long deletedPlayerId;

    public PlayerSelfServiceStepDefs(StepDefs stepDefs, PlayerRepository playerRepository,
                                     GameRepository gameRepository, ScoreRepository scoreRepository,
                                     GameStateRepository gameStateRepository) {
        this.stepDefs = stepDefs;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.scoreRepository = scoreRepository;
        this.gameStateRepository = gameStateRepository;
    }

    @Given("the player {string} exists")
    public void thePlayerExists(String username) {
        ensurePlayer(username);
    }

    @Given("the player {string} exists with a score and a saved game state")
    public void thePlayerExistsWithAScoreAndASavedGameState(String username) {
        Player player = ensurePlayer(username);
        Game game = gameRepository.findById("self-service-game").orElseGet(Game::new);
        game.setId("self-service-game");
        game.setTitle("Self Service Game");
        game.setIsActive(true);
        game = gameRepository.save(game);

        Score score = new Score();
        score.setPlayer(player);
        score.setGame(game);
        score.setScoreValue(42);
        scoreRepository.save(score);

        GameState state = new GameState();
        state.setPlayer(player);
        state.setGame(game);
        state.setPayload("{}");
        gameStateRepository.save(state);
    }

    @When("{string} changes the email of {string} to {string}")
    public void changesTheEmailOf(String actor, String target, String email) throws Throwable {
        patchPlayer(actor, target, Map.of("email", email));
    }

    @When("{string} tries to make themselves an admin")
    public void triesToMakeThemselvesAnAdmin(String username) throws Throwable {
        patchPlayer(username, username, Map.of("admin", true));
    }

    @When("{string} deletes the account of {string}")
    public void deletesTheAccountOf(String actor, String target) throws Throwable {
        Player player = playerRepository.findByUsername(target).orElseThrow();
        deletedPlayerId = player.getId();
        loginAs(actor);
        stepDefs.result = stepDefs.mockMvc.perform(
                delete(player.getUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @Then("the player {string} has the email {string}")
    public void thePlayerHasTheEmail(String username, String email) {
        assertEquals(email, playerRepository.findByUsername(username).orElseThrow().getEmail());
    }

    @Then("the player {string} does not have the email {string}")
    public void thePlayerDoesNotHaveTheEmail(String username, String email) {
        assertNotEquals(email, playerRepository.findByUsername(username).orElseThrow().getEmail());
    }

    @Then("the player {string} is not an admin")
    public void thePlayerIsNotAnAdmin(String username) {
        assertFalse(playerRepository.findByUsername(username).orElseThrow().isAdmin());
    }

    @Then("the player {string} still exists")
    public void thePlayerStillExists(String username) {
        assertTrue(playerRepository.existsByUsername(username));
    }

    @Then("the player {string}, their scores and their game states no longer exist")
    public void thePlayerAndTheirDataNoLongerExist(String username) {
        assertFalse(playerRepository.existsByUsername(username));
        assertTrue(scoreRepository.findByPlayerId(deletedPlayerId).isEmpty());
        assertTrue(gameStateRepository.findByPlayerId(deletedPlayerId).isEmpty());
    }

    private void patchPlayer(String actor, String target, Map<String, Object> changes) throws Exception {
        Player player = playerRepository.findByUsername(target).orElseThrow();
        loginAs(actor);
        stepDefs.result = stepDefs.mockMvc.perform(
                patch(player.getUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(stepDefs.mapper.writeValueAsString(changes))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    private void loginAs(String username) {
        AuthenticationStepDefs.currentUsername = username;
        AuthenticationStepDefs.currentPassword = ADMIN.equals(username) ? ADMIN_PASSWORD : PASSWORD;
        AuthenticationStepDefs.currentToken = null;
    }

    /** (Re)creates the player with a known email and password, so scenarios do not depend on each other. */
    private Player ensurePlayer(String username) {
        Player player = playerRepository.findByUsername(username).orElseGet(Player::new);
        player.setUsername(username);
        player.setEmail(username + "@self.test");
        player.setRawPassword(PASSWORD);
        player.encodePassword();
        player.setAdmin(false);
        return playerRepository.save(player);
    }
}
