@SCRUM-113
Feature: Station Employee Marks Charger Under Maintenance
  As a station employee
  I want to mark a charger as under maintenance
  So that I can prevent users from reserving it until it's repaired

  @SCRUM-165
  Scenario: Successfully mark a charger as under maintenance
    Given a station with an available charger exists
    And I am logged in as a station employee
    When I mark the charger as "UNDER_MAINTENANCE" with note "Routine maintenance"
    Then the charger status should be "UNDER_MAINTENANCE"
    And the charger maintenance note should be "Routine maintenance"
    And the charger should not be available for reservation

  @SCRUM-166
  Scenario: Successfully mark a charger as under maintenance without a note
    Given a station with an available charger exists
    And I am logged in as a station employee
    When I mark the charger as "UNDER_MAINTENANCE" with no note
    Then the charger status should be "UNDER_MAINTENANCE"
    And the charger maintenance note should be empty
    And the charger should not be available for reservation

  @SCRUM-167
  Scenario: Attempt to mark a charger with an invalid status value
    Given a station with an available charger exists
    And I am logged in as a station employee
    When I mark the charger as "IN_USE" with note "Attempting invalid status change"
    Then the response status code should be 400
    And the response body should contain "Invalid status value: IN_USE"

  @SCRUM-168
  Scenario: Attempt to mark a non-existent charger under maintenance
    Given a station with an available charger exists
    And I am logged in as a station employee
    When I mark a non-existent charger as "UNDER_MAINTENANCE" with note "Non-existent charger"
    Then the response status code should be 404
    And the response body should contain "Charger not found"


  @SCRUM-169
  Scenario: Charger marked under maintenance is not available for reservation
    Given a station with an available charger exists
    And the existing charger is marked as "UNDER_MAINTENANCE" with note "Out of order"
    When a user attempts to reserve the charger
    Then the reservation should fail with a message indicating unavailability 