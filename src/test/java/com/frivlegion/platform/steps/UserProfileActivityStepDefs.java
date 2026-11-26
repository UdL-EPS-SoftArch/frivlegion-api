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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserProfileActivityStepDefs {

    private static final String PASSWORD = "password";

    private final StepDefs stepDefs;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final ScoreRepository scoreRepository;

    private Player player;

    public UserProfileActivityStepDefs(StepDefs stepDefs, PlayerRepository playerRepository,
                                       GameRepository gameRepository, ScoreRepository scoreRepository) {
        this.stepDefs = stepDefs;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.scoreRepository = scoreRepository;
    }

    @Given("the authenticated player has recorded scores across 3 games")
    public void theAuthenticatedPlayerHasRecordedScoresAcross3Games() {
        player = playerRepository.findByUsername("player").orElseGet(Player::new);
        player.setUsername("player");
        player.setEmail("player@sample.app");
        player.setRawPassword(PASSWORD);
        player.encodePassword();
        player = playerRepository.save(player);

        scoreRepository.deleteAll(scoreRepository.findByPlayerId(player.getId()));

        saveScore(saveGame("activity-game-1", "Activity Game 1"), 100);
        saveScore(saveGame("activity-game-1", "Activity Game 1"), 250);
        saveScore(saveGame("activity-game-2", "Activity Game 2"), 200);
        saveScore(saveGame("activity-game-3", "Activity Game 3"), 300);

        AuthenticationStepDefs.currentUsername = player.getUsername();
        AuthenticationStepDefs.currentPassword = PASSWORD;
        AuthenticationStepDefs.currentToken = null;
    }

    @When("I request my score history")
    public void iRequestMyScoreHistory() throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get("/scores/search/findByPlayerId?playerId={id}", player.getId())
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()));
    }

    @Then("the score history response lists the recorded score values")
    public void theScoreHistoryResponseListsTheRecordedScoreValues() throws Throwable {
        stepDefs.result.andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.scores", hasSize(4)))
            .andExpect(jsonPath("$._embedded.scores[*].scoreValue",
                containsInAnyOrder(100, 250, 200, 300)))
            .andExpect(jsonPath("$._embedded.scores[*]._links.game.href",
                everyItem(not(emptyOrNullString()))));
    }

    @When("I request my personal best for game {string}")
    public void iRequestMyPersonalBestForGame(String gameId) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get("/scores/search/findFirstByPlayerIdAndGameIdOrderByScoreValueDesc"
                        + "?playerId={playerId}&gameId={gameId}", player.getId(), gameId)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()));
    }

    @Then("the personal best response returns the highest recorded value")
    public void thePersonalBestResponseReturnsTheHighestRecordedValue() throws Throwable {
        stepDefs.result.andExpect(status().isOk())
            .andExpect(jsonPath("$.scoreValue", is(250)));
    }

    private Game saveGame(String id, String title) {
        Game game = gameRepository.findById(id).orElseGet(Game::new);
        game.setId(id);
        game.setTitle(title);
        game.setDescription("Profile activity game");
        game.setThumbnailUrl("https://example.com/" + id + ".png");
        game.setCategory("Casual");
        game.setIsActive(true);
        return gameRepository.save(game);
    }

    private void saveScore(Game game, int value) {
        Score score = new Score();
        score.setPlayer(player);
        score.setGame(game);
        score.setScoreValue(value);
        scoreRepository.save(score);
    }
}
