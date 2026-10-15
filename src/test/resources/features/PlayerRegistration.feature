Feature: US-01 Player Account Registration
Scenario: Successful registration with valid credentials
Given an unauthenticated casual visitor is on the platform
And provides a unique username, a valid email, and a secure password
When they submit the registration form
Then the system should create their player account
And the system should automatically authenticate them by generating a JWT token
Scenario: Registration attempt with a duplicate username
Given the username "Gamer123" already exists in the database
When a visitor attempts to register using the username "Gamer123"
Then the system should reject the registration
And should display an error message indicating the username is already in use
