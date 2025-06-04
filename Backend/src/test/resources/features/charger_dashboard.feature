@SCRUM-112
Feature: Station Employee Charger Dashboard
  As a station employee
  I want to see the status of all chargers at my station
  So that I can monitor operations and know which ones are available

  Background:
    Given I am logged in as a station employee
    And I am viewing the stations dashboard

  @SCRUM-149 @view @happy-path
  Scenario: View list of chargers at the station
    When I request the charger status list
    Then I should see a list of all chargers at my station
    And each charger should have a valid ID and status

  @SCRUM-150 @status @available
  Scenario: View available charger status
    Given there is a charger with ID "CH001"
    When I request the charger status list
    Then I should see charger "CH001" with status "Available"
    And the charger should have a valid price per kWh

  @SCRUM-151 @status @in-use
  Scenario: View in-use charger status
    Given there is a charger with ID "CH002" that is currently in use
    When I request the charger status list
    Then I should see charger "CH002" with status "In use"
    And the charger should have a valid price per kWh

  @SCRUM-152 @status @maintenance
  Scenario: View charger under maintenance
    Given there is a charger with ID "CH003" that is under maintenance
    When I request the charger status list
    Then I should see charger "CH003" with status "Under maintenance"
    And the charger should have a valid price per kWh

  @SCRUM-153 @status @multiple
  Scenario: View multiple chargers with different statuses
    Given there are multiple chargers at my station
    When I request the charger status list
    Then I should see all chargers with their respective statuses
    And each charger should have a valid price per kWh

  @SCRUM-154 @status @count
  Scenario: View charger status counts
    Given there are multiple chargers at my station
    When I request the charger status list
    Then I should see the total count of available chargers
    And I should see the total count of in-use chargers
    And I should see the total count of chargers under maintenance

  @SCRUM-155 @status @filter
  Scenario: Filter chargers by status
    Given there are multiple chargers at my station
    When I request the charger status list
    And I filter chargers by status "Available"
    Then I should only see chargers with status "Available"
    And each charger should have a valid price per kWh

  @SCRUM-156 @status @sort
  Scenario: Sort chargers by status
    Given there are multiple chargers at my station
    When I request the charger status list
    And I sort chargers by status
    Then the chargers should be sorted by status alphabetically
    And each charger should have a valid price per kWh 