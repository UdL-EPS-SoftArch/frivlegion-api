Feature: Game state ownership protection

  Scenario: A player cannot patch another player's game state
    Given a game state owned by "state-owner" exists for game "state-ownership-game"
    When "state-intruder" sends a PATCH to that game state with payload "hacked" and owner "state-intruder"
    Then the game state request is rejected
    And the game state has payload "original" and belongs to "state-owner"

  Scenario: A player cannot delete another player's game state
    Given a game state owned by "state-owner" exists for game "state-ownership-game"
    When "state-intruder" sends a DELETE to that game state
    Then the game state request is rejected
    And the game state has payload "original" and belongs to "state-owner"

  Scenario: The owner cannot hand their game state over to another player
    Given a game state owned by "state-owner" exists for game "state-ownership-game"
    When "state-owner" sends a PATCH to that game state with payload "updated" and owner "state-intruder"
    Then the request is successful
    And the game state has payload "updated" and belongs to "state-owner"
