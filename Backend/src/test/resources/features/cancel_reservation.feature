@SCRUM-22 @reservation
Feature: Cancel Reservation
  As a client
  I want to cancel my reservations
  So that I can manage my charging schedule

  @SCRUM-191 @happy-path @cancel
  Scenario: Client successfully cancels an active reservation
    Given a client is registered with email "client@example.com" and password "securepass123"
    And a station "Station A" exists with an available charger
    And the client has a reservation at that charger with status "ACTIVE"
    When the client cancels the reservation
    Then the reservation should be deleted from the database
    And the response status should be 200

  @SCRUM-192 @negative @not-found
  Scenario: Client attempts to cancel a non-existent reservation
    Given a client is registered with email "client2@example.com" and password "securepass123"
    When the client attempts to cancel a non-existent reservation
    Then the cancellation should fail with status code 404

  @SCRUM-193 @negative @already-cancelled
  Scenario: Client attempts to cancel an already cancelled reservation
    Given a client is registered with email "client3@example.com" and password "securepass123"
    And a station "Station B" exists with an available charger
    And the client has a reservation at that charger with status "COMPLETED"
    When the client attempts to cancel the reservation again
    Then the cancellation should fail with status code 400
    And the response should contain an error message about invalid status
