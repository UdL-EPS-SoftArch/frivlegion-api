package com.frivlegion.platform.steps;

import com.frivlegion.platform.domain.Player;
import com.frivlegion.platform.repository.PlayerRepository;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class PlayerPrivacyStepDefs {

    private final StepDefs stepDefs;
    private final PlayerRepository playerRepository;

    public PlayerPrivacyStepDefs(StepDefs stepDefs, PlayerRepository playerRepository) {
        this.stepDefs = stepDefs;
        this.playerRepository = playerRepository;
    }

    @When("I request the public profile of player {string}")
    public void iRequestThePublicProfileOfPlayer(String username) throws Throwable {
        Player player = playerRepository.findByUsername(username).orElseThrow();
        perform(player.getUri());
    }

    @When("I request the list of players")
    public void iRequestTheListOfPlayers() throws Throwable {
        perform("/players");
    }

    @When("I request my identity")
    public void iRequestMyIdentity() throws Throwable {
        perform("/identity");
    }

    @When("I search a player by the email {string}")
    public void iSearchAPlayerByTheEmail(String email) throws Throwable {
        perform("/players/search/findByEmail?email=" + email);
    }

    @Then("the response shows the username {string} without an email")
    public void theResponseShowsTheUsernameWithoutAnEmail(String username) throws Throwable {
        stepDefs.result
            .andExpect(jsonPath("$.username", is(username)))
            .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Then("no player in the list exposes an email")
    public void noPlayerInTheListExposesAnEmail() throws Throwable {
        stepDefs.result
            .andExpect(jsonPath("$._embedded.players").isNotEmpty())
            .andExpect(content().string(not(containsString("\"email\""))));
    }

    @Then("my identity shows the username {string} and the email {string}")
    public void myIdentityShowsTheUsernameAndTheEmail(String username, String email) throws Throwable {
        stepDefs.result
            .andExpect(jsonPath("$.username", is(username)))
            .andExpect(jsonPath("$.email", is(email)));
    }

    private void perform(String uri) throws Exception {
        stepDefs.result = stepDefs.mockMvc.perform(
                get(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }
}
