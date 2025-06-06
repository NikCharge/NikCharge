@SCRUM-17 @registration @auth
Feature: User Registration
  As a new user
  I want to create an account
  So that I can access the charging station services

  Background:
    Given the system is running

  @SCRUM-63 @happy-path @smoke
  Scenario: Successful user registration with valid data
    When I submit registration with the following details
      | email    | password | name  |
      | test@example.com | Password123! | John Doe |
    Then the registration should be successful
    And I should receive a confirmation message

  @SCRUM-65 @validation @email
  Scenario: Registration fails with invalid email format
    When I submit registration with the following details
      | email    | password | name  |
      | invalid-email | Password123! | John Doe |
    Then the registration should fail
    And I should receive an error message about invalid email format

  @SCRUM-64 @validation @password
  Scenario: Registration fails with weak password
    When I submit registration with the following details
      | email    | password | name  |
      | test@example.com | weak | John Doe |
    Then the registration should fail
    And I should receive an error message about password requirements

  @SCRUM-66 @validation @duplicate
  Scenario: Registration fails with existing email
    Given a user with email "existing@example.com" exists in the system
    When I submit registration with the following details
      | email    | password | name  |
      | existing@example.com | Password123! | John Doe |
    Then the registration should fail
    And I should receive an error message about email already in use 