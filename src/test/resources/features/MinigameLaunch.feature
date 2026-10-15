Feature: US-03 Minigame Launch and Execution
Scenario: Launch a game successfully
Given a user is viewing the game catalog
When they click on a specific game thumbnail
Then the system should load the game inside an isolated iframe container in the main view
And display controls to maximize the screen and to return to the catalog
