Feature: Time-based Discounts for Chargers

  As a station manager
  I want to apply time-based discounts to a station and consequently all of its charger's default price
  So that I can increase usage during off-peak hours without raising prices for users

  Background:
    Given a station with id {long} exists
    And the station has a charger with id {long} and default price {bigdecimal} per kWh

  Scenario: Apply a valid time-based percentage discount
    When the manager applies a {int}% discount to the station with id {long} for {day_of_week} from {int} to {int} hours
    Then the charger with id {long} should have a final price of {bigdecimal} per kWh for a reservation made on {day_of_week} at {localtime}

  Scenario: Ensure final price does not exceed default price
    When the manager applies a {int}% discount to the station with id {long} for {day_of_week} from {int} to {int} hours
    And the calculated price is higher than the default price
    Then the final price for the charger with id {long} for a reservation made on {day_of_week} at {localtime} should be the default price {bigdecimal}

  # More scenarios will be added later for future reservations, overlapping discounts, etc. 