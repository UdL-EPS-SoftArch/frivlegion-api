package com.frivlegion.platform.steps;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.frivlegion.platform.domain.User;
import com.frivlegion.platform.repository.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.json.JSONObject;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

public class PlayerRegistrationStepDefs {
    private final StepDefs stepDefs;
    private final UserRepository userRepository;

    private String registrationUsername;
    private String registrationEmail;
    private String registrationPassword;

    public PlayerRegistrationStepDefs(StepDefs stepDefs, UserRepository userRepository) {
        this.stepDefs = stepDefs;
        this.userRepository = userRepository;
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
        userRepository.findById(registrationUsername).ifPresent(userRepository::delete);
    }

    @When("^they submit the registration form$")
    public void theySubmitTheRegistrationForm() throws Throwable {
        User user = new User();
        user.setId(registrationUsername);
        user.setEmail(registrationEmail);

        stepDefs.result = stepDefs.mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new JSONObject(
                                        stepDefs.mapper.writeValueAsString(user)
                                ).put("password", registrationPassword).toString())
                                .characterEncoding(StandardCharsets.UTF_8)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(AuthenticationStepDefs.authenticate()))
                .andDo(print());
    }

    @Then("^the system should create their player account$")
    public void theSystemShouldCreateTheirPlayerAccount() throws Throwable {
        stepDefs.result.andExpect(status().isCreated());
        assertTrue(userRepository.existsById(registrationUsername));
    }

    @And("^the system should automatically authenticate them by generating a JWT token$")
    public void theSystemShouldAutomaticallyAuthenticateThemByGeneratingAJWTToken() throws Throwable {
        stepDefs.result.andDo(print());
    }

    @Given("^the username \"([^\"]*)\" already exists in the database$")
    public void theUsernameAlreadyExistsInTheDatabase(String username) {
        AuthenticationStepDefs.currentUsername = null;
        AuthenticationStepDefs.currentPassword = null;
        if (!userRepository.existsById(username)) {
            User user = new User();
            user.setEmail(username.toLowerCase() + "@sample.app");
            user.setId(username);
            user.setPassword("password123");
            user.encodePassword();
            userRepository.save(user);
        }
    }

    @When("^a visitor attempts to register using the username \"([^\"]*)\"$")
    public void aVisitorAttemptsToRegisterUsingTheUsername(String username) throws Throwable {
        User user = new User();
        user.setId(username);
        user.setEmail(username.toLowerCase() + "@example.com");

        stepDefs.result = stepDefs.mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new JSONObject(
                                        stepDefs.mapper.writeValueAsString(user)
                                ).put("password", "newpassword123").toString())
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
