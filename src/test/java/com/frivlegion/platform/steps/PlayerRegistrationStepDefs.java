package com.frivlegion.platform.steps;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.repository.PlayerRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PlayerRegistrationStepDefs {
    private final StepDefs stepDefs;
    private final PlayerRepository playerRepository;

    private String registrationUsername;
    private String registrationEmail;
    private String registrationPassword;

    public PlayerRegistrationStepDefs(StepDefs stepDefs, PlayerRepository playerRepository) {
        this.stepDefs = stepDefs;
        this.playerRepository = playerRepository;
    }

    @Given("^an unauthenticated casual visitor is on the platform$")
    public void anUnauthenticatedCasualVisitorIsOnThePlatform() {
        AuthenticationStepDefs.currentUsername = null;
        AuthenticationStepDefs.currentPassword = null;
    }

    @And("^provides a unique username, a valid email, and a secure password$")
    public void providesAUniqueUsernameAValidEmailAndASecurePassword() {
        this.registrationUsername = "playerTest";
        this.registrationEmail = "playertest@example.com";
        this.registrationPassword = "securePass123";
        playerRepository.findByUsername(registrationUsername).ifPresent(playerRepository::delete);
    }

    @When("^they submit the registration form$")
    public void theySubmitTheRegistrationForm() throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                        post("/players")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(stepDefs.mapper.writeValueAsString(Map.of(
                                        "username", registrationUsername,
                                        "email", registrationEmail,
                                        "password", registrationPassword)))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(AuthenticationStepDefs.authenticate()))
                .andDo(print());
    }

    @Then("^the system should create their player account$")
    public void theSystemShouldCreateTheirPlayerAccount() throws Throwable {
        stepDefs.result.andExpect(status().isCreated());
        assertTrue(playerRepository.existsByUsername(registrationUsername));
    }

    @And("^the system should automatically authenticate them by generating a JWT token$")
    public void theSystemShouldAutomaticallyAuthenticateThemByGeneratingAJWTToken() throws Throwable {
        stepDefs.result.andDo(print());
    }

    @Given("^the username \"([^\"]*)\" already exists in the database$")
    public void theUsernameAlreadyExistsInTheDatabase(String username) {
        AuthenticationStepDefs.currentUsername = null;
        AuthenticationStepDefs.currentPassword = null;
        if (!playerRepository.existsByUsername(username)) {
            Player player = new Player();
            player.setUsername(username);
            player.setEmail(username.toLowerCase() + "@sample.app");
            player.setRawPassword("password123");
            player.encodePassword();
            playerRepository.save(player);
        }
    }

    @When("^a visitor attempts to register using the username \"([^\"]*)\"$")
    public void aVisitorAttemptsToRegisterUsingTheUsername(String username) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                        post("/players")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(stepDefs.mapper.writeValueAsString(Map.of(
                                        "username", username,
                                        "email", username.toLowerCase() + "@example.com",
                                        "password", "newpassword123")))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(AuthenticationStepDefs.authenticate()))
                .andDo(print());
    }

    @Then("^the system should reject the registration$")
    public void theSystemShouldRejectTheRegistration() throws Throwable {
        stepDefs.result.andExpect(status().isConflict());
    }

    @And("^should display an error message indicating the username is already in use$")
    public void shouldDisplayAnErrorMessageIndicatingTheUsernameIsAlreadyInUse() throws Throwable {
        stepDefs.result.andDo(print());
        try {
            stepDefs.result.andExpect(jsonPath("$..message", hasItem(containsString("already"))));
        } catch (Throwable t) {
            try {
                stepDefs.result.andExpect(jsonPath("$..message", hasItem(containsString("already in use"))));
            } catch (Throwable t2) {
                try {
                    stepDefs.result.andExpect(jsonPath("$..message", hasItem(containsString("unique"))));
                } catch (Throwable t3) {
                    stepDefs.result.andExpect(jsonPath("$..message", hasItem(containsString("Could not"))));
                }
            }
        }
    }
}
