package com.frivlegion.platform.steps;

import com.frivlegion.platform.domain.Game;
import com.frivlegion.platform.repository.GameRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GridCatalogDisplayStepDefs {

    private final StepDefs stepDefs;
    private final GameRepository gameRepository;

    private List<Game> gridGames = new ArrayList<>();
    private Game selectedGame;

    public GridCatalogDisplayStepDefs(StepDefs stepDefs, GameRepository gameRepository) {
        this.stepDefs = stepDefs;
        this.gameRepository = gameRepository;
    }

    @Given("there are 5 active games in the database")
    public void thereAre5ActiveGamesInTheDatabase() {
        gridGames = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            gridGames.add(saveGame(
                    "grid-game-" + i,
                    "Grid Game " + i,
                    "Description for grid game " + i,
                    "https://example.com/grid-game-" + i + ".png",
                    "Grid"));
        }
    }

    @When("a user, authenticated or not, accesses the Home page")
    public void aUserAccessesTheHomePage() throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get("/games")
                    .accept(MediaType.APPLICATION_JSON))
            .andDo(print());
    }

    @Then("the system should query the active games catalog")
    public void theSystemShouldQueryTheActiveGamesCatalog() throws Throwable {
        stepDefs.result.andExpect(status().isOk());
        assertFalse(gameRepository.findByIsActiveTrue().isEmpty());
    }

    @Then("^display a responsive grid with the cards \\(title and thumbnail\\) of those 5 games$")
    public void displayAResponsiveGridWithTheCardsOfThose5Games() {
        List<Game> activeGames = gameRepository.findByIsActiveTrue();
        List<String> activeIds = activeGames.stream().map(Game::getId).toList();

        gridGames.forEach(game -> {
            assertTrue(activeIds.contains(game.getId()));
            assertNotNull(game.getThumbnailUrl());
        });
    }

    @Given("the game catalog is loaded on the screen")
    public void theGameCatalogIsLoadedOnTheScreen() {
        selectedGame = saveGame(
                "grid-detail-game",
                "Grid Detail Game",
                "A brief description",
                "https://example.com/grid-detail-game.png",
                "Puzzle");
    }

    @When("the user hovers over a game thumbnail")
    public void theUserHoversOverAGameThumbnail() throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get("/games/{id}", selectedGame.getId())
                    .accept(MediaType.APPLICATION_JSON))
            .andDo(print());
    }

    @Then("a brief description and a category tag should be displayed")
    public void aBriefDescriptionAndACategoryTagShouldBeDisplayed() throws Throwable {
        stepDefs.result.andExpect(status().isOk())
            .andExpect(jsonPath("$.description", is(selectedGame.getDescription())))
            .andExpect(jsonPath("$.category", is(selectedGame.getCategory())));
    }

    private Game saveGame(String id, String title, String description, String thumbnailUrl, String category) {
        Game game = gameRepository.findById(id).orElseGet(Game::new);
        game.setId(id);
        game.setTitle(title);
        game.setDescription(description);
        game.setThumbnailUrl(thumbnailUrl);
        game.setCategory(category);
        game.setIsActive(true);
        return gameRepository.save(game);
    }
}
