Feature: Player profile self-service

  Scenario: A player updates their own email
    Given the player "self-alice" exists
    When "self-alice" changes the email of "self-alice" to "alice.new@self.test"
    Then The response code is 200
    And the player "self-alice" has the email "alice.new@self.test"

  Scenario: A player cannot update another player's profile
    Given the player "self-alice" exists
    And the player "self-bob" exists
    When "self-bob" changes the email of "self-alice" to "stolen@self.test"
    Then The response code is 403
    And the player "self-alice" does not have the email "stolen@self.test"

  Scenario: An admin can update another player's profile
    Given the player "self-carol" exists
    When "admin" changes the email of "self-carol" to "carol.fixed@self.test"
    Then The response code is 200
    And the player "self-carol" has the email "carol.fixed@self.test"

  Scenario: A player cannot make themselves an admin
    Given the player "self-alice" exists
    When "self-alice" tries to make themselves an admin
    Then the player "self-alice" is not an admin

  Scenario: A player cannot delete another player's account
    Given the player "self-alice" exists
    And the player "self-bob" exists
    When "self-bob" deletes the account of "self-alice"
    Then The response code is 403
    And the player "self-alice" still exists

  Scenario: A player deletes their own account together with their data
    Given the player "self-dave" exists with a score and a saved game state
    When "self-dave" deletes the account of "self-dave"
    Then the request is successful
    And the player "self-dave", their scores and their game states no longer exist
