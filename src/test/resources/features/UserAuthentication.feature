Feature: US-06 User Authentication and Session Management
Scenario: Successful login with valid credentials
Given a registered user is on the login page
When they enter their valid email and password
And submit the login form
Then the system should authenticate the user
And generate a JWT token for the session
And redirect the user to the Home page
Scenario: Failed login with invalid credentials
Given a registered user is on the login page
When they enter an incorrect password
And submit the login form
Then the system should deny access
And display an error message "Invalid email or password"
Scenario: Successful logout
Given an authenticated user is currently browsing the site
When they click the "Logout" button in the navigation bar
Then the system should invalidate their JWT token
And redirect them to the guest view of the Home page
