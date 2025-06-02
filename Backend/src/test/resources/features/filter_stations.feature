@station @view
Feature: Station View
  As a user
  I want to view station information
  So that I can see details about charging stations

  Background:
    Given the system is running

  @happy-path
  Scenario: View all stations
    Given a station with id 1 exists in the system
    When I request the list of stations
    Then I should receive a list of stations

  @happy-path
  Scenario: View a specific station
    Given a station with id 1 exists in the system
    When I request the station with id 1
    Then I should receive the station details for id 1

  @not-found
  Scenario: View a non-existent station
    When I request the station with id 999
    Then the station view should fail with status 404
    And I should receive an error message about station not found

  @happy-path @filtering
  Scenario: Filter stations by charger type and availability
    Given three stations with two of type "DC_FAST", one available and one in use, and one of a different type
    When I fetch and filter stations for charger type "DC_FAST"
    Then only the available station with type "DC_FAST" is selected
