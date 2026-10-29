package com.frivlegion.platform.steps;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.repository.PlayerRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RegisterStepDefs {
  private final StepDefs stepDefs;
  private final PlayerRepository playerRepository;

  public RegisterStepDefs(StepDefs stepDefs, PlayerRepository playerRepository) {
    this.stepDefs = stepDefs;
    this.playerRepository = playerRepository;
  }

  @Given("^There is no registered user with username \"([^\"]*)\"$")
  public void thereIsNoRegisteredUserWithUsername(String username) {
    assertFalse(playerRepository.existsByUsername(username), "User \"" + username + "\" should not exist");
  }

  @Given("^There is a registered user with username \"([^\"]*)\" and password \"([^\"]*)\" and email \"([^\"]*)\"$")
  public void thereIsARegisteredUserWithUsernameAndPasswordAndEmail(String username, String password, String email) {
    if (!playerRepository.existsByUsername(username)) {
      Player player = new Player();
      player.setUsername(username);
      player.setEmail(email);
      player.setRawPassword(password);
      player.encodePassword();
      playerRepository.save(player);
    }
  }

  @And("^I can login with username \"([^\"]*)\" and password \"([^\"]*)\"$")
  public void iCanLoginWithUsernameAndPassword(String username, String password) throws Throwable {
    AuthenticationStepDefs.currentUsername = username;
    AuthenticationStepDefs.currentPassword = password;

    stepDefs.result = stepDefs.mockMvc.perform(
        get("/identity")
            .accept(MediaType.APPLICATION_JSON)
            .with(AuthenticationStepDefs.authenticate()))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @And("^I cannot login with username \"([^\"]*)\" and password \"([^\"]*)\"$")
  public void iCannotLoginWithUsernameAndPassword(String username, String password) throws Throwable {
    AuthenticationStepDefs.currentUsername = username;
    AuthenticationStepDefs.currentPassword = password;

    stepDefs.result = stepDefs.mockMvc.perform(
        get("/identity")
            .accept(MediaType.APPLICATION_JSON)
            .with(AuthenticationStepDefs.authenticate()))
        .andDo(print())
        .andExpect(status().isUnauthorized());
  }

  @When("^I register a new user with username \"([^\"]*)\", email \"([^\"]*)\" and password \"([^\"]*)\"$")
  public void iRegisterANewUserWithUsernameEmailAndPassword(String username, String email, String password) throws Throwable {
    stepDefs.result = stepDefs.mockMvc.perform(
            post("/players")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(stepDefs.mapper.writeValueAsString(Map.of(
                        "username", username,
                        "email", email,
                        "password", password)))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
  }

  @And("^It has been created a user with username \"([^\"]*)\" and email \"([^\"]*)\", the password is not returned$")
  public void itHasBeenCreatedAUserWithUsername(String username, String email) throws Throwable {
    Player player = playerRepository.findByUsername(username).orElseThrow();

    stepDefs.result = stepDefs.mockMvc.perform(
            get(player.getUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
        .andDo(print())
        .andExpect(jsonPath("$.email", is(email)))
        .andExpect(jsonPath("$.password").doesNotExist())
        .andExpect(jsonPath("$.passwordHash").doesNotExist());
  }

  @And("^It has not been created a user with username \"([^\"]*)\"$")
  public void itHasNotBeenCreatedAUserWithUsername(String username) {
    assertFalse(playerRepository.existsByUsername(username));
  }
}
