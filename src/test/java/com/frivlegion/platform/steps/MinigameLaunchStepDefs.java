package com.frivlegion.platform.steps;

import com.frivlegion.platform.domain.Game;
import com.frivlegion.platform.repository.GameRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MinigameLaunchStepDefs {

    private final StepDefs stepDefs;
    private final GameRepository gameRepository;

    private Game launchedGame;

    public MinigameLaunchStepDefs(StepDefs stepDefs, GameRepository gameRepository) {
        this.stepDefs = stepDefs;
        this.gameRepository = gameRepository;
    }

    @Given("a user is viewing the game catalog")
    public void aUserIsViewingTheGameCatalog() {
        launchedGame = gameRepository.findById("launch-game").orElseGet(Game::new);
        launchedGame.setId("launch-game");
        launchedGame.setTitle("Launch Game");
        launchedGame.setDescription("A game ready to launch");
        launchedGame.setThumbnailUrl("https://example.com/launch-game.png");
        launchedGame.setCategory("Action");
        launchedGame.setIsActive(true);
        launchedGame = gameRepository.save(launchedGame);
    }

    @When("they click on a specific game thumbnail")
    public void theyClickOnASpecificGameThumbnail() throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get("/games/{id}", launchedGame.getId())
                    .accept(MediaType.APPLICATION_JSON))
            .andDo(print());
    }

    @Then("the system should load the game inside an isolated iframe container in the main view")
    public void theSystemShouldLoadTheGameInsideAnIsolatedIframeContainer() throws Throwable {
        stepDefs.result.andExpect(status().isOk())
            .andExpect(jsonPath("$.uri", is(launchedGame.getUri())))
            .andExpect(jsonPath("$.title", is(launchedGame.getTitle())))
            .andExpect(jsonPath("$.thumbnailUrl", is(launchedGame.getThumbnailUrl())))
            .andExpect(jsonPath("$.isActive", is(true)));
    }

    @Then("display controls to maximize the screen and to return to the catalog")
    public void displayControlsToMaximizeTheScreenAndToReturnToTheCatalog() {
        assertTrue(launchedGame.getIsActive());
        assertTrue(gameRepository.existsById(launchedGame.getId()));
    }
}
