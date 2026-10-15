Feature: US-07 Game Catalog Search and Filtering
Scenario: Search for a specific game by name
Given the user is on the Home page with the game catalog loaded
When they type "Mario" into the search bar
Then the catalog grid should update dynamically
And only display games containing "Mario" in their title
Scenario: Filter games by category
Given the user is on the Home page
When they click on the "Puzzle" category filter button
Then the catalog grid should update dynamically
And only display games tagged with the "Puzzle" category
Scenario: Search yields no results
Given the user is on the Home page
When they type "UnknownGame123" into the search bar
Then the catalog grid should be empty
And the system should display a "No games found" message
