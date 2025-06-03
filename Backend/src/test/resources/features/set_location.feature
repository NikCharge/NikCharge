@SCRUM-11
Feature: Set a custom location
  As a user
  I want to change the search location
  So that I can explore availability in another area

  @SCRUM-67 @location @map @happy-path
  Scenario: User sets a new location and views updated station list
    Given there are charging stations in the system
    When the user sets the location to "Aveiro"
    Then the map and station list should include at least one station

  @SCRUM-68 @location @map @reset-location
  Scenario: User resets back to GPS location
    Given there are charging stations in the system
    When the user resets the location to use GPS
    Then the map and list should include at least one station