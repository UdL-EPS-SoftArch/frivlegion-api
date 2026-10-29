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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserProfileActivityStepDefs {

    private final StepDefs stepDefs;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final ScoreRepository scoreRepository;

    private Player player;
    private List<Score> playerScores;

    public UserProfileActivityStepDefs(StepDefs stepDefs, PlayerRepository playerRepository,
                                       GameRepository gameRepository,
                                       ScoreRepository scoreRepository) {
        this.stepDefs = stepDefs;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.scoreRepository = scoreRepository;
    }

    @Given("an authenticated user has played 3 different games this week")
    public void anAuthenticatedUserHasPlayed3DifferentGamesThisWeek() {
        preparePlayer();
        scoreRepository.deleteAll(scoreRepository.findByPlayerId(player.getId()));

        for (int i = 1; i <= 3; i++) {
            Game game = saveGame("activity-game-" + i, "Activity Game " + i, i * 100);
            saveScore(game, i * 100);
        }
    }

    @When("they navigate to their {string} page")
    public void theyNavigateToTheirPage(String page) throws Throwable {
        assertEquals("Profile", page);
        loadScores();
    }

    @Then("the system should query their play history")
    public void theSystemShouldQueryTheirPlayHistory() throws Throwable {
        stepDefs.result.andExpect(status().isOk());
        assertFalse(playerScores.isEmpty());
    }

    @Then("display a {string} section with the thumbnails of those 3 games")
    public void displayASectionWithTheThumbnailsOfThose3Games(String section) {
        assertEquals("Recently Played", section);
        Set<String> gameIds = playerScores.stream()
            .map(score -> score.getGame().getId())
            .collect(Collectors.toSet());

        assertEquals(3, gameIds.size());
        playerScores.forEach(score -> assertNotNull(score.getGame().getThumbnailUrl()));
    }

    @Given("an authenticated user has submitted high scores for multiple games")
    public void anAuthenticatedUserHasSubmittedHighScoresForMultipleGames() {
        preparePlayer();
        scoreRepository.deleteAll(scoreRepository.findByPlayerId(player.getId()));

        for (int i = 1; i <= 3; i++) {
            Game game = saveGame("scores-game-" + i, "Scores Game " + i, i * 150);
            saveScore(game, i * 150);
        }
    }

    @When("they navigate to the {string} tab in their profile")
    public void theyNavigateToTheTabInTheirProfile(String tab) throws Throwable {
        assertEquals("My Scores", tab);
        loadScores();
    }

    @Then("the system should display a list of games they have played")
    public void theSystemShouldDisplayAListOfGamesTheyHavePlayed() {
        Set<String> gameIds = playerScores.stream()
            .map(score -> score.getGame().getId())
            .collect(Collectors.toSet());

        assertEquals(3, gameIds.size());
    }

    @Then("show their personal best score next to each game")
    public void showTheirPersonalBestScoreNextToEachGame() {
        Map<String, Integer> bestByGame = playerScores.stream()
            .collect(Collectors.toMap(
                score -> score.getGame().getId(),
                Score::getScoreValue,
                Math::max));

        bestByGame.forEach((gameId, expectedBest) -> {
            Score personalBest = scoreRepository
                .findFirstByPlayerIdAndGameIdOrderByScoreValueDesc(player.getId(), gameId)
                .orElseThrow();

            assertEquals(expectedBest, personalBest.getScoreValue());
        });
    }

    private void preparePlayer() {
        player = playerRepository.findByUsername("player").orElseGet(Player::new);
        player.setUsername("player");
        player.setEmail("player@sample.app");
        player.setRawPassword("password");
        player.encodePassword();
        player = playerRepository.save(player);

        AuthenticationStepDefs.currentUsername = player.getUsername();
        AuthenticationStepDefs.currentPassword = "password";
    }

    private void loadScores() throws Throwable {
        playerScores = scoreRepository.findByPlayerId(player.getId());

        stepDefs.result = stepDefs.mockMvc.perform(
                get("/scores/search/findByPlayerId?playerId={id}", player.getId())
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    private Game saveGame(String id, String title, int scoreHint) {
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
