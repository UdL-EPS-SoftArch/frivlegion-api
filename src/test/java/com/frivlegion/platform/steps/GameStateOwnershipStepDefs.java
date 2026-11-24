package com.frivlegion.platform.steps;

import com.frivlegion.platform.domain.Game;
import com.frivlegion.platform.domain.GameState;
import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.repository.GameRepository;
import com.frivlegion.platform.repository.GameStateRepository;
import com.frivlegion.platform.repository.PlayerRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class GameStateOwnershipStepDefs {

    private static final String PASSWORD = "password123";

    private final StepDefs stepDefs;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final GameStateRepository gameStateRepository;

    private GameState gameState;

    public GameStateOwnershipStepDefs(StepDefs stepDefs, PlayerRepository playerRepository,
                                      GameRepository gameRepository,
                                      GameStateRepository gameStateRepository) {
        this.stepDefs = stepDefs;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.gameStateRepository = gameStateRepository;
    }

    @Given("a game state owned by {string} exists for game {string}")
    public void aGameStateOwnedByExistsForGame(String username, String gameId) {
        Player owner = ensurePlayer(username);
        Game game = ensureGame(gameId);
        gameStateRepository.findByPlayerIdAndGameId(owner.getId(), gameId)
            .ifPresent(gameStateRepository::delete);

        gameState = new GameState();
        gameState.setPlayer(owner);
        gameState.setGame(game);
        gameState.setPayload("original");
        gameState = gameStateRepository.save(gameState);
    }

    @When("{string} sends a PATCH to that game state with payload {string} and owner {string}")
    public void sendsPatch(String username, String payload, String owner) throws Throwable {
        Player requestedOwner = ensurePlayer(owner);
        String body = stepDefs.mapper.writeValueAsString(Map.of(
            "payload", payload,
            "player", requestedOwner.getUri()));

        loginAs(username);
        stepDefs.result = stepDefs.mockMvc.perform(
                patch("/gameStates/{id}", gameState.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @When("{string} sends a DELETE to that game state")
    public void sendsDelete(String username) throws Throwable {
        loginAs(username);
        stepDefs.result = stepDefs.mockMvc.perform(
                delete("/gameStates/{id}", gameState.getId())
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    /** Other players' states are invisible (404) or explicitly denied (403). */
    @Then("the game state request is rejected")
    public void theGameStateRequestIsRejected() {
        int status = stepDefs.result.andReturn().getResponse().getStatus();
        assertTrue(status == 403 || status == 404, "Expected 403 or 404 but got " + status);
    }

    @Then("the game state has payload {string} and belongs to {string}")
    public void theGameStateHasPayloadAndBelongsTo(String payload, String username) {
        GameState stored = gameStateRepository
            .findByPlayerIdAndGameId(playerRepository.findByUsername(username).orElseThrow().getId(),
                gameState.getGame().getId())
            .orElseThrow();
        assertEquals(gameState.getId(), stored.getId());
        assertEquals(payload, stored.getPayload());
    }

    private void loginAs(String username) {
        ensurePlayer(username);
        AuthenticationStepDefs.currentUsername = username;
        AuthenticationStepDefs.currentPassword = PASSWORD;
        AuthenticationStepDefs.currentToken = null;
    }

    private Player ensurePlayer(String username) {
        Player player = playerRepository.findByUsername(username).orElseGet(Player::new);
        player.setUsername(username);
        player.setEmail(username + "@state.test");
        player.setRawPassword(PASSWORD);
        player.encodePassword();
        return playerRepository.save(player);
    }

    private Game ensureGame(String gameId) {
        Game game = gameRepository.findById(gameId).orElseGet(Game::new);
        game.setId(gameId);
        game.setTitle("State Ownership Game");
        game.setDescription("Game used for game state ownership tests");
        game.setThumbnailUrl("https://example.com/" + gameId + ".png");
        game.setCategory("Casual");
        game.setIsActive(true);
        return gameRepository.save(game);
    }
}
