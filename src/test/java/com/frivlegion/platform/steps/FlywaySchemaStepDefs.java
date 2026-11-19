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

    private static PostgreSQLContainer postgres;
    private static ConfigurableApplicationContext context;

    private JdbcTemplate jdbcTemplate;

    @Before("@flyway")
    public void requireDocker() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is required for PostgreSQL schema validation");
    }

    @Given("PostgreSQL is available for schema validation")
    public void postgresqlIsAvailableForSchemaValidation() {
        if (postgres == null) {
            postgres = new PostgreSQLContainer("postgres:17-alpine");
            postgres.start();
        }
    }

    @When("the application starts with Flyway and Hibernate schema validation")
    public void theApplicationStartsWithFlywayAndHibernateSchemaValidation() {
        if (context == null) {
            context = new SpringApplicationBuilder(FrivLegionApplication.class)
                    .web(WebApplicationType.NONE)
                    .profiles("integration-test")
                    .properties(
                            "spring.datasource.url=" + postgres.getJdbcUrl(),
                            "spring.datasource.username=" + postgres.getUsername(),
                            "spring.datasource.password=" + postgres.getPassword(),
                            "spring.datasource.driver-class-name=" + postgres.getDriverClassName())
                    .run();
        }
        jdbcTemplate = context.getBean(JdbcTemplate.class);
    }

    @Then("the migration is recorded as successful")
    public void theMigrationIsRecordedAsSuccessful() {
        Integer applied = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where version = '1' and success = true",
                Integer.class);
        assertEquals(1, applied);
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
}
