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

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GameStateSavingStepDefs {

    private final StepDefs stepDefs;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final GameStateRepository gameStateRepository;

    private Player player;
    private Game game;
    private GameState savedState;
    private String savedPayload;
    private long stateCountBefore;

    public GameStateSavingStepDefs(StepDefs stepDefs, GameRepository gameRepository,
                                   PlayerRepository playerRepository,
                                   GameStateRepository gameStateRepository) {
        this.stepDefs = stepDefs;
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.gameStateRepository = gameStateRepository;
    }

    @Given("a registered player is playing a compatible game with a valid session")
    public void aRegisteredPlayerIsPlayingACompatibleGameWithAValidSession() {
        authenticatePlayer();
        game = saveGame("compatible-game", "Compatible Game");
    }

    @When("the game reaches a checkpoint and the minigame SDK sends a {string} payload")
    public void theGameReachesACheckpointAndSendsAPayload(String event) throws Throwable {
        savedPayload = "{\"level\":3,\"checkpoint\":\"forest\"}";

        stepDefs.result = submitState(savedPayload);
    }

    @Then("the system should store the JSON payload in the database linked to the user and the game")
    public void theSystemShouldStoreTheJsonPayloadLinkedToTheUserAndTheGame() {
        GameState state = gameStateRepository
            .findFirstByPlayerIdAndGameIdOrderByUpdatedAtDesc(player.getId(), game.getId())
            .orElseThrow();
        assertEquals(savedPayload, state.getPayload());
        assertEquals(player.getId(), state.getPlayer().getId());
        assertEquals(game.getId(), state.getGame().getId());
    }

    @Then("return a success confirmation to the minigame")
    public void returnASuccessConfirmationToTheMinigame() throws Throwable {
        stepDefs.result.andExpect(status().isCreated());
    }

    @Given("a registered player has previously saved progress for {string}")
    public void aRegisteredPlayerHasPreviouslySavedProgressFor(String title) {
        authenticatePlayer();
        game = saveGame(slug(title), title);

        gameStateRepository.deleteAll(
            gameStateRepository.findByPlayerIdAndGameId(player.getId(), game.getId()));

        savedPayload = "{\"level\":7,\"checkpoint\":\"castle\"}";
        savedState = new GameState();
        savedState.setPlayer(player);
        savedState.setGame(game);
        savedState.setPayload(savedPayload);
        savedState = gameStateRepository.save(savedState);
    }

    @When("they launch {string} from the catalog")
    public void theyLaunchFromTheCatalog(String title) throws Throwable {
        assertNotNull(savedState);

        stepDefs.result = stepDefs.mockMvc.perform(
                get(savedState.getUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @Then("the system should retrieve their latest saved state from the database")
    public void theSystemShouldRetrieveTheirLatestSavedState() throws Throwable {
        stepDefs.result.andExpect(status().isOk())
            .andExpect(jsonPath("$.payload", is(savedPayload)));
    }

    @Then("inject the state payload into the minigame iframe upon initialization")
    public void injectTheStatePayloadIntoTheMinigameIframe() throws Throwable {
        stepDefs.result.andExpect(jsonPath("$.payload", is(savedPayload)));
    }

    @Given("an unauthenticated visitor is playing a game")
    public void anUnauthenticatedVisitorIsPlayingAGame() {
        AuthenticationStepDefs.currentUsername = null;
        AuthenticationStepDefs.currentPassword = null;
        game = saveGame("guest-game", "Guest Game");
        stateCountBefore = gameStateRepository.count();
    }

    @When("the game attempts to trigger a {string} event")
    public void theGameAttemptsToTriggerAnEvent(String event) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                post("/gameStates")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @Then("the system should intercept the request")
    public void theSystemShouldInterceptTheRequest() throws Throwable {
        int status = stepDefs.result.andReturn().getResponse().getStatus();
        assertTrue(status == 401 || status == 403);
    }

    @Then("display a modal inviting the user to log in or register to save their progress")
    public void displayAModalInvitingTheUserToLogInOrRegister() throws Throwable {
        int status = stepDefs.result.andReturn().getResponse().getStatus();
        assertTrue(status == 401 || status == 403);
        assertEquals(stateCountBefore, gameStateRepository.count(),
            "A guest save attempt must not persist game state");
    }

    private void authenticatePlayer() {
        player = playerRepository.findByUsername("player").orElseGet(Player::new);
        player.setUsername("player");
        player.setEmail("player@sample.app");
        player.setRawPassword("password");
        player.encodePassword();
        player = playerRepository.save(player);

        AuthenticationStepDefs.currentUsername = player.getUsername();
        AuthenticationStepDefs.currentPassword = "password";
    }

    private org.springframework.test.web.servlet.ResultActions submitState(String payload) throws Exception {
        String body = stepDefs.mapper.writeValueAsString(Map.of(
            "player", player.getUri(),
            "game", game.getUri(),
            "payload", payload));

        return stepDefs.mockMvc.perform(
                post("/gameStates")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    private Game saveGame(String id, String title) {
        Game game = gameRepository.findById(id).orElseGet(Game::new);
        game.setId(id);
        game.setTitle(title);
        game.setDescription("State saving game");
        game.setThumbnailUrl("https://example.com/" + id + ".png");
        game.setCategory("Adventure");
        game.setIsActive(true);
        return gameRepository.save(game);
    }

    private String slug(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
