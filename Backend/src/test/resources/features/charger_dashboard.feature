@SCRUM-112
Feature: Station Employee Charger Dashboard
  As a station employee
  I want to see the status of all chargers at my station
  So that I can monitor operations and know which ones are available

  Background:
    Given I am logged in as a station employee
    And I am viewing my station's dashboard

  Scenario: View list of chargers at the station
    When I request the charger status list
    Then I should see a list of all chargers at my station

  Scenario: View available charger status
    Given there is a charger with ID "CH001"
    When I request the charger status list
    Then I should see charger "CH001" with status "Available"

  Scenario: View in-use charger status
    Given there is a charger with ID "CH002" that is currently in use
    When I request the charger status list
    Then I should see charger "CH002" with status "In use"

  Scenario: View charger under maintenance
    Given there is a charger with ID "CH003" that is under maintenance
    When I request the charger status list
    Then I should see charger "CH003" with status "Under maintenance"

  Scenario: View multiple chargers with different statuses
    Given there are multiple chargers at my station
    When I request the charger status list
    Then I should see all chargers with their respective statuses 