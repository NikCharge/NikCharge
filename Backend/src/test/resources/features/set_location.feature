@SCRUM-11
Feature: Set a custom location
  As a user
  I want to change the search location
  So that I can explore availability in another area

  @SCRUM-67 @location @map @happy-path
  Scenario: User sets a new location and views updated station list
    When I set my search location to "Aveiro"
    Then the map and station list should update to show results near "Aveiro"
    And all distances should be relative to "Aveiro"

  @SCRUM-68 @location @map @reset-location
  Scenario: User resets back to GPS location
    Given my current search location is "Lisbon"
    When I switch back to "this location"
    Then the map and list should show results based on my GPS position