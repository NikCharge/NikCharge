@SCRUM-21 @view_reservations @view
Feature: View Reservations
  As a logged-in client
  I want to view my reservations
  So that I can see my upcoming and past charging sessions

  Scenario: View active reservations when logged in
    Given a client is registered
    And a station with name "Station Active" and address "Address Active" and city "City Active" exists
    And a charger with type "AC_STANDARD" and status "AVAILABLE" at station "Station Active" exists
    And the client has a reservation at that charger with status "ACTIVE"
    When the client requests to view their reservations
    Then the response status should be 200
    And the response should contain exactly 1 reservation
    And one reservation should have status "ACTIVE"
    And the "ACTIVE" reservation should include station name "Station Active" and address "Address Active" and city "City Active"

  Scenario: View completed reservations when logged in
    Given a client is registered
    And a station with name "Station Completed" and address "Address Completed" and city "City Completed" exists
    And a charger with type "DC_FAST" and status "AVAILABLE" at station "Station Completed" exists
    And the client has a reservation at that charger with status "COMPLETED"
    When the client requests to view their reservations
    Then the response status should be 200
    And the response should contain exactly 1 reservation
    And one reservation should have status "COMPLETED"
    And the "COMPLETED" reservation should include station name "Station Completed" and address "Address Completed" and city "City Completed"
