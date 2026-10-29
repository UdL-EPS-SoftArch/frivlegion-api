package com.frivlegion.platform.steps;

import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.repository.PlayerRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserAuthenticationStepDefs {

    private static final String AUTH_USERNAME = "auth-user";
    private static final String AUTH_USER_EMAIL = "auth-user@sample.app";
    private static final String AUTH_USER_PASSWORD = "password123";

    private final StepDefs stepDefs;
    private final PlayerRepository playerRepository;

    private Player authPlayer;

    public UserAuthenticationStepDefs(StepDefs stepDefs, PlayerRepository playerRepository) {
        this.stepDefs = stepDefs;
        this.playerRepository = playerRepository;
    }

    @Given("a registered user is on the login page")
    public void aRegisteredUserIsOnTheLoginPage() {
        authPlayer = ensurePlayer();
    }

    @When("they enter their valid email and password")
    public void theyEnterTheirValidEmailAndPassword() {
        authPlayer = ensurePlayer();
        AuthenticationStepDefs.currentUsername = authPlayer.getUsername();
        AuthenticationStepDefs.currentPassword = AUTH_USER_PASSWORD;
    }

    @When("they enter an incorrect password")
    public void theyEnterAnIncorrectPassword() {
        authPlayer = ensurePlayer();
        AuthenticationStepDefs.currentUsername = authPlayer.getUsername();
        AuthenticationStepDefs.currentPassword = "wrong-password";
    }

    @And("submit the login form")
    public void submitTheLoginForm() throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get("/identity")
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @Then("the system should authenticate the user")
    public void theSystemShouldAuthenticateTheUser() throws Throwable {
        stepDefs.result.andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is(AUTH_USERNAME)))
            .andExpect(jsonPath("$.email", is(AUTH_USER_EMAIL)));
    }

    @Then("generate a JWT token for the session")
    public void generateAJwtTokenForTheSession() throws Throwable {
        stepDefs.result.andExpect(status().isOk())
            .andExpect(jsonPath("$.username").exists());
    }

    @Then("redirect the user to the Home page")
    public void redirectTheUserToTheHomePage() throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get("/games")
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Then("the system should deny access")
    public void theSystemShouldDenyAccess() throws Throwable {
        stepDefs.result.andExpect(status().isUnauthorized());
    }

    @Then("display an error message {string}")
    public void displayAnErrorMessage(String message) throws Throwable {
        assertEquals("Invalid email or password", message);
        stepDefs.result.andExpect(status().isUnauthorized());
    }

    @Given("an authenticated user is currently browsing the site")
    public void anAuthenticatedUserIsCurrentlyBrowsingTheSite() {
        authPlayer = ensurePlayer();
        AuthenticationStepDefs.currentUsername = authPlayer.getUsername();
        AuthenticationStepDefs.currentPassword = AUTH_USER_PASSWORD;
    }

    @When("they click the {string} button in the navigation bar")
    public void theyClickOnTheButtonInTheNavigationBar(String button) throws Throwable {
        assertEquals("Logout", button);
        AuthenticationStepDefs.currentUsername = null;
        AuthenticationStepDefs.currentPassword = null;

        stepDefs.result = stepDefs.mockMvc.perform(
                get("/identity")
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @Then("the system should invalidate their JWT token")
    public void theSystemShouldInvalidateTheirJwtToken() throws Throwable {
        stepDefs.result.andExpect(status().isUnauthorized());
    }

    @Then("redirect them to the guest view of the Home page")
    public void redirectThemToTheGuestViewOfTheHomePage() throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get("/games")
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print())
            .andExpect(status().isOk());
    }

    private Player ensurePlayer() {
        Player player = playerRepository.findByUsername(AUTH_USERNAME).orElseGet(Player::new);
        player.setUsername(AUTH_USERNAME);
        player.setEmail(AUTH_USER_EMAIL);
        player.setRawPassword(AUTH_USER_PASSWORD);
        player.encodePassword();
        return playerRepository.save(player);
    }
}
