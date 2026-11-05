package com.frivlegion.platform.steps;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AuthenticationStepDefs {

    public static String currentUsername;
    public static String currentPassword;
    public static String currentToken;

    private static MockMvc mockMvc;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        currentUsername = "";
        currentPassword = "";
        currentToken = null;
    }

    static void setMockMvc(MockMvc mvc) {
        mockMvc = mvc;
    }

    static RequestPostProcessor authenticate() {
        if (currentToken == null && currentUsername != null && !currentUsername.isBlank()) {
            try {
                currentToken = login(mockMvc, currentUsername, currentPassword);
            } catch (Exception exception) {
                currentToken = null;
            }
        }

        if (currentToken != null && !currentToken.isBlank()) {
            String token = currentToken;
            return request -> {
                request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                return request;
            };
        }

        return anonymous();
    }

    static String login(MockMvc mvc, String usernameOrEmail, String password) throws Exception {
        MvcResult result = mvc.perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(Map.of(
                        "username", usernameOrEmail,
                        "password", password == null ? "" : password)))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON))
            .andReturn();

        if (result.getResponse().getStatus() != 200) {
            return null;
        }

        JsonNode response = mapper.readTree(
            result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        JsonNode token = response.get("token");
        return token == null || token.isNull() ? null : token.asText();
    }

    @Given("^I login as \"([^\"]*)\" with password \"([^\"]*)\"$")
    public void iLoginAsWithPassword(String username, String password) {
        currentUsername = username;
        currentPassword = password;
        currentToken = null;
    }

    @Given("^I'm not logged in$")
    public void iMNotLoggedIn() {
        currentUsername = null;
        currentPassword = null;
        currentToken = null;
    }
}
