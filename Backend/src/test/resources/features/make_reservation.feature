@SCRUM-18 @reservation
Feature: Reservation creation

  @SCRUM-143 @happy-path @reserve
  Scenario: Client successfully makes a reservation
    Given a client is registered with email "client@example.com" and password "securepass123"
    And a station "Station A" exists with an available charger
    When the client reserves a charger at "2025-06-05T10:00:00"
    Then the reservation is created successfully with status "ACTIVE"

  @SCRUM-144 @edge-case @no-charger
  Scenario: Client attempts to reserve when no chargers are available
    Given a client is registered with email "client2@example.com" and password "securepass123"
    And a station "Empty Station" exists without available chargers
    When the client reserves a charger at "2025-06-06T10:00:00"
    Then the reservation creation should fail with status code 400

  @SCRUM-145 @edge-case @overlap
  Scenario: Client tries to make overlapping reservations
    Given a client is registered with email "client3@example.com" and password "securepass123"
    And a station "Overlap Station" exists with an available charger
    And a reservation already exists at "2025-06-07T10:00:00"
    When the client reserves a charger at "2025-06-07T10:00:00"
    Then the reservation is created successfully with status "ACTIVE"

  @SCRUM-146 @negative @invalid
  Scenario: Client attempts reservation with a non-existent charger
    Given a client is registered with email "client4@example.com" and password "securepass123"
    When the client tries to reserve with a non-existent charger at "2025-06-08T10:00:00"
    Then the reservation creation should fail with status code 400

  @SCRUM-147 @negative @validation
  Scenario: Client sends a reservation with missing fields
    Given a client is registered with email "client5@example.com" and password "securepass123"
    And a station "Faulty Station" exists with an available charger
    When the client submits an incomplete reservation
    Then the reservation creation should fail with status code 400
