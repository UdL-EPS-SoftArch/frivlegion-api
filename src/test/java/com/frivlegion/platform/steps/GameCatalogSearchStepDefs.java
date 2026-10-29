package com.frivlegion.platform.steps;

import com.frivlegion.platform.domain.Game;
import com.frivlegion.platform.repository.GameRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GameCatalogSearchStepDefs {

    private final StepDefs stepDefs;
    private final GameRepository gameRepository;

    private List<Game> catalogResults = new ArrayList<>();

    public GameCatalogSearchStepDefs(StepDefs stepDefs, GameRepository gameRepository) {
        this.stepDefs = stepDefs;
        this.gameRepository = gameRepository;
    }

    @Given("the user is on the Home page with the game catalog loaded")
    public void theUserIsOnTheHomePageWithTheGameCatalogLoaded() {
        loadCatalog();
    }

    @Given("the user is on the Home page")
    public void theUserIsOnTheHomePage() {
        loadCatalog();
    }

    @When("they type {string} into the search bar")
    public void theyTypeIntoTheSearchBar(String title) throws Throwable {
        catalogResults = gameRepository.findByTitleContainingIgnoreCase(title);

        stepDefs.result = stepDefs.mockMvc.perform(
                get("/games/search/findByTitleContainingIgnoreCase?title={title}", title)
                    .accept(MediaType.APPLICATION_JSON))
            .andDo(print());
    }

    @Then("the catalog grid should update dynamically")
    public void theCatalogGridShouldUpdateDynamically() throws Throwable {
        stepDefs.result.andExpect(status().isOk());
        assertNotNull(catalogResults);
    }

    @Then("only display games containing {string} in their title")
    public void onlyDisplayGamesContainingInTheirTitle(String title) {
        assertFalse(catalogResults.isEmpty());
        catalogResults.forEach(game ->
            assertTrue(game.getTitle().toLowerCase().contains(title.toLowerCase())));
    }

    @When("they click on the {string} category filter button")
    public void theyClickOnTheCategoryFilterButton(String category) throws Throwable {
        catalogResults = gameRepository.findByCategoryIgnoreCase(category);

        stepDefs.result = stepDefs.mockMvc.perform(
                get("/games/search/findByCategoryIgnoreCase?category={category}", category)
                    .accept(MediaType.APPLICATION_JSON))
            .andDo(print());
    }

    @Then("only display games tagged with the {string} category")
    public void onlyDisplayGamesTaggedWithTheCategory(String category) {
        assertFalse(catalogResults.isEmpty());
        catalogResults.forEach(game ->
            assertTrue(game.getCategory().equalsIgnoreCase(category)));
    }

    @Then("the catalog grid should be empty")
    public void theCatalogGridShouldBeEmpty() {
        assertTrue(catalogResults.isEmpty());
    }

    @Then("the system should display a {string} message")
    public void theSystemShouldDisplayAMessage(String message) throws Throwable {
        assertEquals("No games found", message);
        stepDefs.result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.games", empty()));
        assertTrue(catalogResults.isEmpty());
    }

    private void loadCatalog() {
        saveGame("catalog-mario-kart", "Mario Kart", "Racing game", "https://example.com/mario-kart.png", "Racing");
        saveGame("catalog-super-mario", "Super Mario", "Platform game", "https://example.com/super-mario.png", "Platform");
        saveGame("catalog-puzzle-quest", "Puzzle Quest", "Puzzle game", "https://example.com/puzzle-quest.png", "Puzzle");
        saveGame("catalog-tetris", "Tetris", "Classic puzzle", "https://example.com/tetris.png", "Puzzle");
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
