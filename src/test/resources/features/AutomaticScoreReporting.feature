Feature: US-04 Automatic Score Reporting
Scenario: Save a new score that beats the previous record
Given a registered player is logged in with a valid JWT token
And their current high score in "La Oca" is 1000
When the minigame SDK invokes the score submission event with a value of 1500 upon finishing the game
Then the system should validate the player's session and token
And the system should update the personal record with the new score of 1500
