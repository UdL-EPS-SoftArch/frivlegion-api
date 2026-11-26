package com.frivlegion.platform.steps;

import com.frivlegion.platform.domain.Game;
import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.domain.Score;
import com.frivlegion.platform.repository.GameRepository;
import com.frivlegion.platform.repository.PlayerRepository;
import com.frivlegion.platform.repository.ScoreRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ScoreOwnershipStepDefs {

    private static final String PASSWORD = "password123";
    private static final String ADMIN = "admin";
    private static final String ADMIN_PASSWORD = "password";

    private final StepDefs stepDefs;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final ScoreRepository scoreRepository;

    private Score score;

    public ScoreOwnershipStepDefs(StepDefs stepDefs, PlayerRepository playerRepository,
                                  GameRepository gameRepository, ScoreRepository scoreRepository) {
        this.stepDefs = stepDefs;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.scoreRepository = scoreRepository;
    }

    @Given("a score owned by {string} exists for game {string}")
    public void aScoreOwnedByExistsForGame(String username, String gameId) {
        Player owner = ensurePlayer(username);
        Game game = ensureGame(gameId);
        scoreRepository.deleteAll(scoreRepository.findByGameId(gameId));

        score = new Score();
        score.setPlayer(owner);
        score.setGame(game);
        score.setScoreValue(100);
        score = scoreRepository.save(score);
    }

    @When("{string} sends a PATCH to that score with value {int} and owner {string}")
    public void sendsPatch(String username, int value, String owner) throws Throwable {
        Player requestedOwner = ensurePlayer(owner);
        String body = stepDefs.mapper.writeValueAsString(Map.of(
            "scoreValue", value,
            "player", requestedOwner.getUri()));

        loginAs(username);
        stepDefs.result = stepDefs.mockMvc.perform(
                patch("/scores/{id}", score.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @When("{string} sends a PUT to that score with value {int} and owner {string}")
    public void sendsPut(String username, int value, String owner) throws Throwable {
        Player requestedOwner = ensurePlayer(owner);
        String body = stepDefs.mapper.writeValueAsString(Map.of(
            "game", score.getGame().getUri(),
            "scoreValue", value,
            "player", requestedOwner.getUri()));

        loginAs(username);
        stepDefs.result = stepDefs.mockMvc.perform(
                put("/scores/{id}", score.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @When("{string} sends a DELETE to that score")
    public void sendsDelete(String username) throws Throwable {
        loginAs(username);
        stepDefs.result = stepDefs.mockMvc.perform(
                delete("/scores/{id}", score.getId())
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @Then("the request is forbidden")
    public void theRequestIsForbidden() throws Throwable {
        stepDefs.result.andExpect(status().isForbidden());
    }

    @Then("the request is successful")
    public void theRequestIsSuccessful() throws Throwable {
        stepDefs.result.andExpect(status().is2xxSuccessful());
    }

    @When("an authenticated player sends a DELETE to the scores collection")
    public void sendsBulkDelete() throws Throwable {
        loginAs("score-owner");
        stepDefs.result = stepDefs.mockMvc.perform(
                delete("/scores")
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @Then("bulk score deletion is not available")
    public void bulkScoreDeletionIsNotAvailable() throws Throwable {
        stepDefs.result.andExpect(status().isNotFound());
    }

    @Then("the score deletion is not exported")
    public void theScoreDeletionIsNotExported() throws Throwable {
        stepDefs.result.andExpect(status().isMethodNotAllowed());
    }

    @Then("the score has value {int} and belongs to {string}")
    public void theScoreHasValueAndBelongsTo(int value, String username) {
        Score stored = scoreRepository.findById(score.getId()).orElseThrow();
        assertEquals(value, stored.getScoreValue());
        assertEquals(username, stored.getPlayer().getUsername());
    }

    private void loginAs(String username) {
        if (ADMIN.equals(username)) {
            ensureAdmin();
            AuthenticationStepDefs.currentPassword = ADMIN_PASSWORD;
        } else {
            ensurePlayer(username);
            AuthenticationStepDefs.currentPassword = PASSWORD;
        }
        AuthenticationStepDefs.currentUsername = username;
        AuthenticationStepDefs.currentToken = null;
    }

    private void ensureAdmin() {
        Player admin = playerRepository.findByUsername(ADMIN).orElseGet(Player::new);
        admin.setUsername(ADMIN);
        admin.setEmail("admin@sample.app");
        admin.setRawPassword(ADMIN_PASSWORD);
        admin.encodePassword();
        admin.setAdmin(true);
        playerRepository.save(admin);
    }

    private Player ensurePlayer(String username) {
        Player player = playerRepository.findByUsername(username).orElseGet(Player::new);
        player.setUsername(username);
        player.setEmail(username + "@score.test");
        player.setRawPassword(PASSWORD);
        player.encodePassword();
        return playerRepository.save(player);
    }

    private Game ensureGame(String gameId) {
        Game game = gameRepository.findById(gameId).orElseGet(Game::new);
        game.setId(gameId);
        game.setTitle("Score Ownership Game");
        game.setDescription("Game used for score ownership tests");
        game.setThumbnailUrl("https://example.com/" + gameId + ".png");
        game.setCategory("Casual");
        game.setIsActive(true);
        return stepDefs.asAdmin(() -> gameRepository.save(game));
    }
}
