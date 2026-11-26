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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LeaderboardQueryStepDefs {

    private final StepDefs stepDefs;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final ScoreRepository scoreRepository;

    private String gameId;

    public LeaderboardQueryStepDefs(StepDefs stepDefs, GameRepository gameRepository,
                                    PlayerRepository playerRepository, ScoreRepository scoreRepository) {
        this.stepDefs = stepDefs;
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.scoreRepository = scoreRepository;
    }

    @Given("scores of 800, 1000 and 1200 are recorded for game {string}")
    public void scoresAreRecordedForGame(String title) {
        gameId = slug(title);
        saveGame(gameId, title);
        scoreRepository.deleteAll(scoreRepository.findByGameId(gameId));

        saveScore("lb-alice", 1000);
        saveScore("lb-bob", 1200);
        saveScore("lb-carol", 800);
    }

    @When("I request the leaderboard for game {string}")
    public void iRequestTheLeaderboardForGame(String title) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get("/scores/search/findByGameIdOrderByScoreValueDesc?gameId={id}", slug(title))
                    .accept(MediaType.APPLICATION_JSON));
    }

    @Then("the leaderboard response lists the scores in descending order")
    public void theLeaderboardResponseListsTheScoresInDescendingOrder() throws Throwable {
        stepDefs.result.andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.scores[*].scoreValue", contains(1200, 1000, 800)));
    }

    @Then("every leaderboard entry exposes its player and score value")
    public void everyLeaderboardEntryExposesItsPlayerAndScoreValue() throws Throwable {
        stepDefs.result.andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.scores[*]._links.player.href",
                everyItem(not(emptyOrNullString()))))
            .andExpect(jsonPath("$._embedded.scores[*].scoreValue",
                everyItem(notNullValue())));
    }

    private void saveGame(String id, String title) {
        Game game = gameRepository.findById(id).orElseGet(Game::new);
        game.setId(id);
        game.setTitle(title);
        game.setDescription("Leaderboard game");
        game.setThumbnailUrl("https://example.com/" + id + ".png");
        game.setCategory("Casual");
        game.setIsActive(true);
        stepDefs.asAdmin(() -> gameRepository.save(game));
    }

    private void saveScore(String username, int value) {
        Player player = playerRepository.findByUsername(username).orElseGet(Player::new);
        player.setUsername(username);
        player.setEmail(username + "@example.com");
        player.setPasswordHash("not-used");
        player = playerRepository.save(player);

        Score score = new Score();
        score.setPlayer(player);
        score.setGame(gameRepository.findById(gameId).orElseThrow());
        score.setScoreValue(value);
        scoreRepository.save(score);
    }

    private String slug(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
