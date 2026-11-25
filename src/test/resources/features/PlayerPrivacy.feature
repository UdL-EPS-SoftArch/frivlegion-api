Feature: Player privacy (emails are never public)

  Scenario: Another player's profile does not expose the email
    Given I login as "player" with password "password"
    When I request the public profile of player "admin"
    Then The response code is 200
    And the response shows the username "admin" without an email

  Scenario: An anonymous visitor sees only the public profile
    Given I'm not logged in
    When I request the public profile of player "player"
    Then The response code is 200
    And the response shows the username "player" without an email

  Scenario: A standard player cannot list every player
    Given I login as "player" with password "password"
    When I request the list of players
    Then The response code is 403

  Scenario: An admin lists players without emails
    Given I login as "admin" with password "password"
    When I request the list of players
    Then The response code is 200
    And no player in the list exposes an email

  Scenario: A player reads their own complete profile
    Given I login as "player" with password "password"
    When I request my identity
    Then The response code is 200
    And my identity shows the username "player" and the email "player@sample.app"

  Scenario: Players cannot be searched by email
    Given I login as "player" with password "password"
    When I search a player by the email "admin@sample.app"
    Then The response code is 404
