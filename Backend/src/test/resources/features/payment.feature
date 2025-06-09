@SCRUM-27 @payment
Feature: Payment for Reservations
  As a user I want to be able to pay for my charging reservations
  So that I can complete my charging session
  
  Background:
    Given a client is registered with email "user@test.com" and password "pass123"
    And a station "Station One" exists with an available charger
    And a station "Station Two" exists with an available charger
    And the client has a reservation at that charger with status "ACTIVE"
    And the client has a reservation at that charger with status "COMPLETED"

  @SCRUM-204 @happy-path @active
  Scenario: Pay for an active reservation
    When I attempt to initiate payment for the active reservation

  @SCRUM-205 @happy-path @completed
  Scenario: Pay for a completed reservation
    When I attempt to initiate payment for the completed reservation

  @SCRUM-206 @already-paid
  Scenario: Attempt to pay for an already paid reservation
    And the active reservation is already paid
    When I attempt to initiate payment for the active reservation
    Then the response status should be 400

  @SCRUM-207 @status
  Scenario: Verify payment status after payment
    Given I have successfully paid for the completed reservation
    When I fetch the completed reservation
    Then I should see "Paid" status for the completed reservation
