@SCRUM-28 @view_completed_reservations
Feature: View Completed Reservations

  As a registered client,
  I want to view my completed reservations
  So that I can keep track of my past charging sessions

  @SCRUM-197 @happy-path @completed
  Scenario: Successfully viewing completed reservations when there are completed reservations
    Given a client is registered with email "client@example.com" and password "securepass123"
    And a station "Past Station" exists with an available charger
    And the client has a reservation at that charger with status "COMPLETED"
    When the client requests to view their completed reservations
    Then the response should contain exactly 1 reservation(s)
    And one reservation should have status "COMPLETED"

  @SCRUM-198 @no_completed
  Scenario: Viewing completed reservations when there are no completed reservations
    Given a client is registered with email "client@example.com" and password "securepass123"
    When the client requests to view their completed reservations
    Then the response should contain exactly 0 reservation(s)