Feature: US-05 Leaderboard query API
    In order to compare results
    As a platform visitor
    I want to retrieve the top scores of a game through the REST API

    Scenario: Retrieve the top scores of a game
        Given scores of 800, 1000 and 1200 are recorded for game "La Oca"
        When I request the leaderboard for game "La Oca"
        Then the leaderboard response lists the scores in descending order
        And every leaderboard entry exposes its player and score value
