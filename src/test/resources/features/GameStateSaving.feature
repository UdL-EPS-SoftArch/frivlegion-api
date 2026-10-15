Feature: US-08 Game State Saving and Loading (Progress)
Scenario: Save game progress for an authenticated user
Given a registered player is playing a compatible game with a valid session
When the game reaches a checkpoint and the minigame SDK sends a "save_state" payload
Then the system should store the JSON payload in the database linked to the user and the game
And return a success confirmation to the minigame
Scenario: Load game progress for an authenticated user
Given a registered player has previously saved progress for "Adventure Quest"
When they launch "Adventure Quest" from the catalog
Then the system should retrieve their latest saved state from the database
And inject the state payload into the minigame iframe upon initialization
Scenario: Prompt guest user to register when attempting to save
Given an unauthenticated visitor is playing a game
When the game attempts to trigger a "save_state" event
Then the system should intercept the request
And display a modal inviting the user to log in or register to save their progress
