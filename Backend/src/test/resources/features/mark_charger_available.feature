@SCRUM-114
Feature: Mark Charger as Available Again
  As a station employee
  I want to mark a charger as available again
  So that I can restore it for user reservations

  Background:
    Given I am logged in as a station employee

  @SCRUM-182
  Scenario: Successfully mark a charger as available
    Given a station with a charger that is "UNDER_MAINTENANCE" exists
    When I mark the charger as "AVAILABLE"
    Then the response status code should be 200
    And the charger status should be "AVAILABLE"
    And the charger should be available for reservation
    And the charger maintenance note should be empty 