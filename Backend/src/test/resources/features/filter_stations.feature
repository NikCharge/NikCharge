@SCRUM-13 @filtering @chargers
Feature: Filter stations by charger type
  As a user
  I want to filter by charger type
  So that I only see availability for the type my EV supports

  Background:
    Given there are multiple stations with chargers of various types

  @SCRUM-122 @ac-only
  Scenario: Filter stations with Standard (AC) chargers
    When I filter stations by charger type "AC_STANDARD"
    Then I should only see stations that have "AC_STANDARD" chargers

  @SCRUM-123 @dc-fast
  Scenario: Filter stations with Fast (DC) chargers
    When I filter stations by charger type "DC_FAST"
    Then I should only see stations that have "DC_FAST" chargers

  @SCRUM-124 @ultra-dc
  Scenario: Filter stations with Ultra-fast (DC) chargers
    When I filter stations by charger type "DC_ULTRA_FAST"
    Then I should only see stations that have "DC_ULTRA_FAST" chargers
