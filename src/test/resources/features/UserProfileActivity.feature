Feature: US-09 Player score history API
    In order to review my activity
    As an authenticated player
    I want to retrieve my scores and personal bests through the REST API

    Background:
        Given the authenticated player has recorded scores across 3 games

    Scenario: Retrieve my play history
        When I request my score history
        Then the score history response lists the recorded score values

    Scenario: Retrieve my personal best for a game
        When I request my personal best for game "activity-game-1"
        Then the personal best response returns the highest recorded value
