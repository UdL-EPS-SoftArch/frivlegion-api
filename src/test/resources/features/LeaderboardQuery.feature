Feature: US-05 Leaderboard Query
Scenario: View the top 10 of a game
Given there are recorded scores for the game "La Oca"
When a player accesses the "La Oca" leaderboard view
Then the system should retrieve the top scores
And display a descending ordered list with the top players and their scores
