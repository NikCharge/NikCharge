@edit @profile
Feature: Edit User Profile
  As a registered user
  I want to update my profile information
  So that I can keep my account details accurate

  Background:
    Given the system is running

  @happy-path
  Scenario: Successfully update user profile
    Given a user with email "edituser@example.com" exists in the system
    When I update the profile for "edituser@example.com" with the following data
      | name         | email               | batteryCapacityKwh | fullRangeKm |
      | New Name     | newuser@example.com | 85                 | 420         |
    Then the profile should be updated successfully
    And the response should contain the updated profile data

  @not-found
  Scenario: Attempt to update a non-existent user
    When I update the profile for "ghost@example.com" with the following data
      | name     | email            | batteryCapacityKwh | fullRangeKm |
      | Ghost    | ghost@example.com | 70                 | 350         |
    Then the update should fail with status 404
    And I should receive an error message about user not found
