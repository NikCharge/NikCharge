@auth @login
Feature: User Login
  As a registered user
  I want to log into the system
  So that I can access my account

  Background:
    Given the system is running

  @happy-path @smoke
  Scenario: Successful login with valid credentials
    Given a user with email "user@example.com" exists in the system
    When I submit login with the following credentials
      | email            | password       |
      | user@example.com | Password123!   |
    Then the login should be successful
    And I should receive an authentication token

  @invalid-password
  Scenario: Login fails with wrong password
    Given a user with email "wrongpass@example.com" exists in the system
    When I submit login with the following credentials
      | email              | password     |
      | wrongpass@example.com | wrongpass   |
    Then the login should be forbidden
    And I should receive an error message about invalid credentials

  @non-existent-user
  Scenario: Login fails with non-existent email
    When I submit login with the following credentials
      | email              | password       |
      | notfound@example.com | Password123! |
    Then the login should be forbidden
    And I should receive an error message about invalid credentials

  @missing-fields
  Scenario: Login fails with missing password
    When I submit login with the following credentials
      | email              |
      | missing@example.com |
    Then the login should fail
    And I should receive an error message about missing fields
