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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LeaderboardQueryStepDefs {

    private final StepDefs stepDefs;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final ScoreRepository scoreRepository;

    private Game leaderboardGame;
    private List<Score> leaderboard = new ArrayList<>();

    public LeaderboardQueryStepDefs(StepDefs stepDefs, GameRepository gameRepository,
                                    PlayerRepository playerRepository, ScoreRepository scoreRepository) {
        this.stepDefs = stepDefs;
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.scoreRepository = scoreRepository;
    }

    @Given("there are recorded scores for the game {string}")
    public void thereAreRecordedScoresForTheGame(String title) {
        leaderboardGame = saveGame(slug(title), title);

        scoreRepository.deleteAll(scoreRepository.findByGameId(leaderboardGame.getId()));

        Player alice = savePlayer("lb-alice", "lb-alice@example.com");
        Player bob = savePlayer("lb-bob", "lb-bob@example.com");
        Player carol = savePlayer("lb-carol", "lb-carol@example.com");

        saveScore(alice, leaderboardGame, 1000);
        saveScore(bob, leaderboardGame, 1200);
        saveScore(carol, leaderboardGame, 800);
    }

    @When("a player accesses the {string} leaderboard view")
    public void aPlayerAccessesTheLeaderboardView(String title) throws Throwable {
        leaderboardGame = gameRepository.findById(slug(title)).orElseThrow();
        leaderboard = scoreRepository.findByGameIdOrderByScoreValueDesc(leaderboardGame.getId());

        stepDefs.result = stepDefs.mockMvc.perform(
                get("/scores/search/findByGameIdOrderByScoreValueDesc?gameId={id}", leaderboardGame.getId())
                    .accept(MediaType.APPLICATION_JSON))
            .andDo(print());
    }

    @Then("the system should retrieve the top scores")
    public void theSystemShouldRetrieveTheTopScores() throws Throwable {
        stepDefs.result.andExpect(status().isOk());
        assertFalse(leaderboard.isEmpty());
    }

    @Then("display a descending ordered list with the top players and their scores")
    public void displayADescendingOrderedListWithTheTopPlayersAndTheirScores() {
        for (int i = 1; i < leaderboard.size(); i++) {
            assertTrue(leaderboard.get(i - 1).getScoreValue() >= leaderboard.get(i).getScoreValue());
        }
        leaderboard.forEach(score -> {
            assertNotNull(score.getPlayer());
            assertNotNull(score.getPlayer().getUsername());
        });
    }

    private Game saveGame(String id, String title) {
        Game game = gameRepository.findById(id).orElseGet(Game::new);
        game.setId(id);
        game.setTitle(title);
        game.setDescription("Leaderboard game");
        game.setThumbnailUrl("https://example.com/" + id + ".png");
        game.setCategory("Casual");
        game.setIsActive(true);
        return gameRepository.save(game);
    }

    private Player savePlayer(String username, String email) {
        Player player = playerRepository.findByUsername(username).orElseGet(Player::new);
        player.setUsername(username);
        player.setEmail(email);
        player.setPasswordHash("not-used");
        return playerRepository.save(player);
    }

    private void saveScore(Player player, Game game, int value) {
        Score score = new Score();
        score.setPlayer(player);
        score.setGame(game);
        score.setScoreValue(value);
        scoreRepository.save(score);
    }

    private String slug(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
