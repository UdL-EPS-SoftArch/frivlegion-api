package com.frivlegion.platform.steps;

import com.frivlegion.platform.domain.Game;
import com.frivlegion.platform.repository.GameRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GameCatalogSearchStepDefs {

    private final StepDefs stepDefs;
    private final GameRepository gameRepository;

    public GameCatalogSearchStepDefs(StepDefs stepDefs, GameRepository gameRepository) {
        this.stepDefs = stepDefs;
        this.gameRepository = gameRepository;
    }

    @Given("the catalog contains searchable sample games")
    public void theCatalogContainsSearchableSampleGames() {
        saveGame("catalog-mario-kart", "Mario Kart", "Racing game", "Racing");
        saveGame("catalog-super-mario", "Super Mario", "Platform game", "Platform");
        saveGame("catalog-puzzle-quest", "Puzzle Quest", "Puzzle game", "Puzzle");
        saveGame("catalog-tetris", "Tetris", "Classic puzzle", "Puzzle");
    }

    @When("I search the game catalog for {string}")
    public void iSearchTheGameCatalogFor(String title) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get("/games/search/findByTitleContainingIgnoreCase?title={title}", title)
                    .accept(MediaType.APPLICATION_JSON));
    }

    @Then("the catalog search response contains only games whose title includes {string}")
    public void theCatalogSearchResponseContainsOnlyGamesWhoseTitleIncludes(String title) throws Throwable {
        stepDefs.result.andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.games", hasSize(2)))
            .andExpect(jsonPath("$._embedded.games[*].title", everyItem(containsString(title))));
    }

    @When("I filter the game catalog by category {string}")
    public void iFilterTheGameCatalogByCategory(String category) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get("/games/search/findByCategoryIgnoreCase?category={category}", category)
                    .accept(MediaType.APPLICATION_JSON));
    }

    @Then("the catalog search response contains only games in category {string}")
    public void theCatalogSearchResponseContainsOnlyGamesInCategory(String category) throws Throwable {
        stepDefs.result.andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.games", hasSize(2)))
            .andExpect(jsonPath("$._embedded.games[*].category", everyItem(equalToIgnoringCase(category))));
    }

    @Then("the catalog search response is empty")
    public void theCatalogSearchResponseIsEmpty() throws Throwable {
        stepDefs.result.andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.games", empty()));
    }

    private void saveGame(String id, String title, String description, String category) {
        Game game = gameRepository.findById(id).orElseGet(Game::new);
        game.setId(id);
        game.setTitle(title);
        game.setDescription(description);
        game.setThumbnailUrl("https://example.com/" + id + ".png");
        game.setCategory(category);
        game.setIsActive(true);
        gameRepository.save(game);
    }
}
