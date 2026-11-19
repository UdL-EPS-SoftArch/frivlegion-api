@flyway
Feature: Flyway schema validation
  Scenario: The initial migration creates the expected columns
    Given PostgreSQL is available for schema validation
    When the application starts with Flyway and Hibernate schema validation
    Then the migration is recorded as successful
    And every entity table has a uri column
