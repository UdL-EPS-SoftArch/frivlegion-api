@flyway
Feature: Flyway schema validation
  Scenario: The initial migration matches the entity mappings
    Given PostgreSQL is available for schema validation
    When the application starts with Flyway and Hibernate schema validation
    Then all migrations are recorded as successful
    And no entity table persists a uri column
