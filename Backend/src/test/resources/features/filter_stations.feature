@SCRUM-13 @filtering @chargers
Feature: Filter stations by charger type and availability
  As a user
  I want to filter by charger type and availability
  So that I only see stations that support my EV and are available

  Background:
    Given there are multiple stations with chargers of various types and statuses

  @SCRUM-122 @ac-only
  Scenario: Filter stations with Standard (AC) chargers available
    When I filter stations by charger type "AC_STANDARD" and status "AVAILABLE"
    Then I should only see stations that have "AC_STANDARD" chargers with status "AVAILABLE"

  @SCRUM-123 @dc-fast
  Scenario: Filter stations with Fast (DC) chargers available
    When I filter stations by charger type "DC_FAST" and status "AVAILABLE"
    Then I should only see stations that have "DC_FAST" chargers with status "AVAILABLE"
