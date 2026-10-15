Feature: US-09 User Profile and Activity History
Scenario: View recently played games
Given an authenticated user has played 3 different games this week
When they navigate to their "Profile" page
Then the system should query their play history
And display a "Recently Played" section with the thumbnails of those 3 games
Scenario: View personal high scores
Given an authenticated user has submitted high scores for multiple games
When they navigate to the "My Scores" tab in their profile
Then the system should display a list of games they have played
And show their personal best score next to each game
