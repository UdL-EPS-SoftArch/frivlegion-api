Feature: US-07 Game catalog search API
    In order to find games to play
    As a platform visitor
    I want to query the catalog through its REST search endpoints

    Scenario: Search games by title
        Given the catalog contains searchable sample games
        When I search the game catalog for "Mario"
        Then the catalog search response contains only games whose title includes "Mario"

    Scenario: Filter games by category
        Given the catalog contains searchable sample games
        When I filter the game catalog by category "Puzzle"
        Then the catalog search response contains only games in category "Puzzle"

    Scenario: Search with no matches
        Given the catalog contains searchable sample games
        When I search the game catalog for "UnknownGame123"
        Then the catalog search response is empty
