package com.frivlegion.platform.steps;

import io.cucumber.java.en.When;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class DefaultAuthenticationStepDefs {

    private final StepDefs stepDefs;

    public DefaultAuthenticationStepDefs(StepDefs stepDefs) {
        this.stepDefs = stepDefs;
    }

    @When("I send an anonymous GET to {string}")
    public void iSendAnAnonymousGetTo(String uri) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }

    @When("I try to create a player directly through {string}")
    public void iTryToCreateAPlayerDirectlyThrough(String uri) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                post(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(stepDefs.mapper.writeValueAsString(Map.of(
                        "username", "bypass-user",
                        "email", "bypass@sample.app",
                        "password", "password123")))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
    }
}
