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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AutomaticScoreReportingStepDefs {

    private final StepDefs stepDefs;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final ScoreRepository scoreRepository;

    private Player player;
    private Game game;

    public AutomaticScoreReportingStepDefs(StepDefs stepDefs, GameRepository gameRepository,
                                           PlayerRepository playerRepository, ScoreRepository scoreRepository) {
        this.stepDefs = stepDefs;
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.scoreRepository = scoreRepository;
    }

    @Given("a registered player is logged in with a valid JWT token")
    public void aRegisteredPlayerIsLoggedInWithAValidJwtToken() {
        player = playerRepository.findByUsername("player").orElseGet(Player::new);
        player.setUsername("player");
        player.setEmail("player@sample.app");
        player.setRawPassword("password");
        player.encodePassword();
        player = playerRepository.save(player);

        AuthenticationStepDefs.currentUsername = player.getUsername();
        AuthenticationStepDefs.currentPassword = "password";
    }

    @Given("their current high score in {string} is {int}")
    public void theirCurrentHighScoreInIs(String title, int value) {
        game = saveGame(slug(title), title);
        scoreRepository.deleteAll(scoreRepository.findByPlayerIdAndGameId(player.getId(), game.getId()));

        Score score = new Score();
        score.setPlayer(player);
        score.setGame(game);
        score.setScoreValue(value);
        scoreRepository.save(score);
    }

    @When("the minigame SDK invokes the score submission event with a value of {int} upon finishing the game")
    public void theMinigameSdkInvokesTheScoreSubmissionEvent(int value) throws Throwable {
        String body = stepDefs.mapper.writeValueAsString(Map.of(
            "player", player.getUri(),
            "game", game.getUri(),
            "scoreValue", value));

        stepDefs.result = stepDefs.mockMvc.perform(
                post("/scores")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @Then("the system should validate the player's session and token")
    public void theSystemShouldValidateThePlayersSessionAndToken() throws Throwable {
        stepDefs.result.andExpect(status().isCreated());
    }

    @Then("the system should update the personal record with the new score of {int}")
    public void theSystemShouldUpdateThePersonalRecordWithTheNewScore(int value) {
        Score best = scoreRepository
            .findFirstByPlayerIdAndGameIdOrderByScoreValueDesc(player.getId(), game.getId())
            .orElseThrow();
        assertEquals(value, best.getScoreValue());
    }

    private Game saveGame(String id, String title) {
        Game game = gameRepository.findById(id).orElseGet(Game::new);
        game.setId(id);
        game.setTitle(title);
        game.setDescription("Score reporting game");
        game.setThumbnailUrl("https://example.com/" + id + ".png");
        game.setCategory("Casual");
        game.setIsActive(true);
        return gameRepository.save(game);
    }

    private String slug(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
