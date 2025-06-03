@SCRUM-12 @time-picker @calendar @availability
Feature: Set a custom time
  As a user
  I want to choose a future date and time using a calendar
  So that I can check availability when I plan to charge

  Background:
    Given there are charging stations with available chargers

  @SCRUM-127 @calendar-selection
  Scenario: User selects a future date and time from calendar picker
    When the user opens the calendar picker
    And selects "tomorrow at 10:00"
    Then the station availability should update to reflect that selected time

  @SCRUM-128 @reset-to-now
  Scenario: User resets back to current real-time availability
    Given the user previously selected a custom time
    When the user selects the option "Current time"
    Then the availability should update to show real-time data
