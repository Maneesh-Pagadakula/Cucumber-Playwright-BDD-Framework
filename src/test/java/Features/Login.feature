Feature: Login to OrangeHRM
  As a user, I want to log in to the OrangeHRM application so I can manage HR tasks.
  
  Scenario Outline: User Login - <Scenario>
    Given I navigate to the OrangeHRM login page
    When I enter credentials for "<Username>" and "<Password>" with flag "<Flag>"
    And I click on the login button
    Then I should see the "<ExpectedResult>" message

  Examples:
    | Flag | Scenario      | Username  | Password  | ExpectedResult         |
    |  X   | Valid login   | Admin     | admin123  | Dashboard              |
    |      | Invalid login | wrongUser | wrongPass | Invalid credentials    |
    

    