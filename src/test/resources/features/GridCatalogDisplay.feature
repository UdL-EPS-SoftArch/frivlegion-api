Feature: US-02 Grid Catalog Display
Scenario: Default display of active games
Given there are 5 active games in the database
When a user, authenticated or not, accesses the Home page
Then the system should query the active games catalog
And display a responsive grid with the cards (title and thumbnail) of those 5 games
Scenario: Game details on interaction
Given the game catalog is loaded on the screen
When the user hovers over a game thumbnail
Then a brief description and a category tag should be displayed
