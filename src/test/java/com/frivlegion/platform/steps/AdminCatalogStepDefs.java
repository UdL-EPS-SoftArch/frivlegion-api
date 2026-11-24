package com.frivlegion.platform.steps;

import com.frivlegion.platform.domain.Game;
import com.frivlegion.platform.repository.GameRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class AdminCatalogStepDefs {

    private final StepDefs stepDefs;
    private final GameRepository gameRepository;

    public AdminCatalogStepDefs(StepDefs stepDefs, GameRepository gameRepository) {
        this.stepDefs = stepDefs;
        this.gameRepository = gameRepository;
    }

    @Given("the catalog game {string} exists")
    public void theCatalogGameExists(String id) {
        Game game = gameRepository.findById(id).orElseGet(Game::new);
        game.setId(id);
        game.setTitle("Old Game");
        game.setCategory("Puzzle");
        game.setIsActive(true);
        gameRepository.save(game);
    }

    @When("I add the game {string} titled {string} to the catalog")
    public void iAddTheGameToTheCatalog(String id, String title) throws Throwable {
        String body = stepDefs.mapper.writeValueAsString(Map.of(
            "title", title,
            "description", "Added through the API",
            "thumbnailUrl", "https://example.com/" + id + ".png",
            "category", "Arcade",
            "isActive", true));
        // Game ids are client-assigned slugs, so the game is created with PUT /games/{id}.
        stepDefs.result = stepDefs.mockMvc.perform(
                put("/games/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @When("I rename the catalog game {string} to {string}")
    public void iRenameTheCatalogGame(String id, String title) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                patch("/games/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(stepDefs.mapper.writeValueAsString(Map.of("title", title)))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @When("I remove the catalog game {string}")
    public void iRemoveTheCatalogGame(String id) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                delete("/games/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @When("{string} logs in with password {string}")
    public void logsInWithPassword(String username, String password) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(stepDefs.mapper.writeValueAsString(Map.of(
                        "username", username, "password", password)))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON))
            .andDo(print());
    }

    @Then("the returned roles include {string}")
    public void theReturnedRolesInclude(String role) throws Throwable {
        stepDefs.result.andExpect(jsonPath("$.roles", hasItem(role)));
    }

    @Then("the catalog contains the game {string}")
    public void theCatalogContainsTheGame(String id) {
        assertTrue(gameRepository.existsById(id));
    }

    @Then("the catalog does not contain the game {string}")
    public void theCatalogDoesNotContainTheGame(String id) {
        assertFalse(gameRepository.existsById(id));
    }
}
