Feature: Role-based catalog management (Admin vs. standard player)

  Scenario: An admin adds a game to the catalog
    Given I login as "admin" with password "password"
    When I add the game "admin-new-game" titled "Admin New Game" to the catalog
    Then The response code is 201
    And the catalog contains the game "admin-new-game"

  Scenario: An admin updates and removes a game
    Given the catalog game "admin-old-game" exists
    And I login as "admin" with password "password"
    When I rename the catalog game "admin-old-game" to "Renamed Game"
    Then The response code is 200
    When I remove the catalog game "admin-old-game"
    Then the request is successful
    And the catalog does not contain the game "admin-old-game"

  Scenario: A standard player cannot add a game
    Given I login as "player" with password "password"
    When I add the game "player-game" titled "Player Game" to the catalog
    Then The response code is 403
    And the catalog does not contain the game "player-game"

  Scenario: An anonymous visitor cannot add a game
    Given I'm not logged in
    When I add the game "anonymous-game" titled "Anonymous Game" to the catalog
    Then The response code is 401

  Scenario: The login response tells the client the player's roles
    When "admin" logs in with password "password"
    Then The response code is 200
    And the returned roles include "ROLE_ADMIN"
