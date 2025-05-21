Feature: View nearby chargers on map

  Scenario: Show map and list of chargers
    Given the user has opened the app
    And location permission has been granted
    When the default location is used
    Then the map displays chargers as pins with availability count
    And the list displays stations sorted by distance
