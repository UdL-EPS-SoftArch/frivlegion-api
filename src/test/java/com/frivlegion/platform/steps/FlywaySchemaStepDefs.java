package com.frivlegion.platform.steps;

import com.frivlegion.platform.FrivLegionApplication;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class FlywaySchemaStepDefs {

    private static final String DB_URL_SETTING = "FRIVLEGION_TEST_DB_URL";
    private static final String DB_USERNAME_SETTING = "FRIVLEGION_TEST_DB_USERNAME";
    private static final String DB_PASSWORD_SETTING = "FRIVLEGION_TEST_DB_PASSWORD";

    private static PostgreSQLContainer postgres;
    private static ConfigurableApplicationContext context;

    private JdbcTemplate jdbcTemplate;

    @Before("@flyway")
    public void requireDatabase() {
        if (setting(DB_URL_SETTING) == null) {
            assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
                "Docker or " + DB_URL_SETTING + " is required for PostgreSQL schema validation");
        }
    }

    @Given("PostgreSQL is available for schema validation")
    public void postgresqlIsAvailableForSchemaValidation() {
        if (setting(DB_URL_SETTING) == null && postgres == null) {
            postgres = new PostgreSQLContainer("postgres:17-alpine");
            postgres.start();
        }
    }

    @When("the application starts with Flyway and Hibernate schema validation")
    public void theApplicationStartsWithFlywayAndHibernateSchemaValidation() {
        if (context == null) {
            SpringApplicationBuilder builder = new SpringApplicationBuilder(FrivLegionApplication.class)
                    .web(WebApplicationType.SERVLET)
                    .profiles("integration-test")
                    .properties("server.port=0");

            if (setting(DB_URL_SETTING) != null) {
                builder.properties(
                        "spring.datasource.url=" + setting(DB_URL_SETTING),
                        "spring.datasource.username=" + settingOrDefault(DB_USERNAME_SETTING, "postgres"),
                        "spring.datasource.password=" + settingOrDefault(DB_PASSWORD_SETTING, "postgres"),
                        "spring.datasource.driver-class-name=org.postgresql.Driver");
            } else {
                builder.properties(
                        "spring.datasource.url=" + postgres.getJdbcUrl(),
                        "spring.datasource.username=" + postgres.getUsername(),
                        "spring.datasource.password=" + postgres.getPassword(),
                        "spring.datasource.driver-class-name=" + postgres.getDriverClassName());
            }
            context = builder.run();
        }
        jdbcTemplate = context.getBean(JdbcTemplate.class);
    }

    @Then("all migrations are recorded as successful")
    public void allMigrationsAreRecordedAsSuccessful() {
        for (String version : List.of("1", "2", "3", "4")) {
            Integer applied = jdbcTemplate.queryForObject(
                    "select count(*) from flyway_schema_history where version = ? and success = true",
                    Integer.class, version);
            assertEquals(1, applied, () -> "Migration " + version + " was not applied successfully");
        }
        assertEquals("validate", context.getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto"));
    }

    @Then("no entity table persists a uri column")
    public void noEntityTablePersistsAUriColumn() {
        for (String table : List.of("player", "game", "game_state", "record", "score")) {
            List<String> columns = jdbcTemplate.queryForList(
                    "select column_name from information_schema.columns where table_name = ?",
                    String.class, table);
            assertFalse(columns.contains("uri"), () -> table + " still persists the uri column");
        }
    }

    @AfterAll
    public static void stopPostgres() {
        if (context != null) {
            context.close();
            context = null;
        }
        if (postgres != null) {
            postgres.stop();
            postgres = null;
        }
    }

    private static String setting(String name) {
        String value = System.getProperty(name);
        if (value == null || value.isBlank()) {
            value = System.getenv(name);
        }
        return value == null || value.isBlank() ? null : value;
    }

    private static String settingOrDefault(String name, String defaultValue) {
        String value = setting(name);
        return value == null ? defaultValue : value;
    }
}
