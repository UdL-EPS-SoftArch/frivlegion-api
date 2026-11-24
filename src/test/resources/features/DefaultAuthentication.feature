Feature: Authentication required by default

  Scenario: Protected resources reject anonymous requests
    Given I'm not logged in
    When I send an anonymous GET to "/gameStates"
    Then The response code is 401

  Scenario: Routes that are not explicitly public reject anonymous requests
    Given I'm not logged in
    When I send an anonymous GET to "/records/search/findByName?name=anything"
    Then The response code is 401

  Scenario: The public catalog stays open to visitors
    Given I'm not logged in
    When I send an anonymous GET to "/games"
    Then The response code is 200

  Scenario: Players cannot be created bypassing /auth/register
    Given I'm not logged in
    When I try to create a player directly through "/players"
    Then The response code is 405
    And It has not been created a user with username "bypass-user"
