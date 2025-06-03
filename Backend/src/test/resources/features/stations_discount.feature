@SCRUM-15 @discount @search
Feature: Highlight discounted stations in search results
  As a user
  I want to see which stations have a discount for my selected time
  So that I can take advantage of cheaper charging options

  Background:
    Given the system is running
    And a discount-search station with id 1 exists
    And a charger of type "DC_FAST" exists for discount-search station 1
    And a 15% discount is active now for discount-search station 1 with charger type "DC_FAST"

  @SCRUM-15-1 @happy-path
  Scenario: Discounted station appears in list view
    When I search for stations at hour 12 with charger type "DC_FAST"
    And I get the station list for discount search
    Then discount-search station 1 should show a discount tag with value "15"

  @SCRUM-15-2 @filter-update
  Scenario: Discount tag disappears when no charger types match
    Given a discount-search station with id 2 exists
    And a charger of type "AC_STANDARD" exists for discount-search station 2
    And no discounts are currently active for discount-search station 2
    When I search for stations at hour 12 with charger type "AC_STANDARD"
    And I get the station list for discount search
    Then discount-search station 2 should show no discount tag
