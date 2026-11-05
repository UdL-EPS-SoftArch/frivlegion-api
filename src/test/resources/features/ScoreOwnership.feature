Feature: Score ownership protection

  Scenario: A player cannot patch another player's score
    Given a score owned by "score-owner" exists for game "score-ownership-game"
    When "score-intruder" sends a PATCH to that score with value 9999 and owner "score-intruder"
    Then the request is forbidden
    And the score has value 100 and belongs to "score-owner"

  Scenario: A player cannot replace another player's score
    Given a score owned by "score-owner" exists for game "score-ownership-game"
    When "score-intruder" sends a PUT to that score with value 9999 and owner "score-intruder"
    Then the request is forbidden
    And the score has value 100 and belongs to "score-owner"

  Scenario: A player cannot delete another player's score
    Given a score owned by "score-owner" exists for game "score-ownership-game"
    When "score-intruder" sends a DELETE to that score
    Then the request is forbidden
    And the score has value 100 and belongs to "score-owner"

  Scenario: The owner can update their own score without changing ownership
    Given a score owned by "score-owner" exists for game "score-ownership-game"
    When "score-owner" sends a PATCH to that score with value 200 and owner "score-intruder"
    Then the request is successful
    And the score has value 200 and belongs to "score-owner"
