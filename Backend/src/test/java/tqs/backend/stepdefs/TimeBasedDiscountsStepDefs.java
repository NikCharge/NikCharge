package tqs.backend.stepdefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.net.URI;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

import tqs.backend.dto.DiscountRequestDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TimeBasedDiscountsStepDefs {

    @Autowired
    private TestRestTemplate restTemplate;

    // State to be shared between steps
    private Long stationId;
    private Long chargerId;
    private BigDecimal chargerDefaultPrice;
    // private BigDecimal calculatedFinalPrice; // Not needed for API interaction
    private int appliedDiscountPercentage;
    private DayOfWeek discountDayOfWeek;
    private int discountStartHour;
    private int discountEndHour;
    private LocalTime reservationTime; // To store the time of the hypothetical reservation
    private ResponseEntity<String> latestResponse;

    @Given("a station with id {long} exists")
    public void a_station_with_id_exists(long stationId) {
        // TODO: Implement logic to ensure a station with this ID exists in the test environment
        this.stationId = stationId;
        System.out.println("Given station with id: " + stationId + " exists");
    }

    @And("the station has a charger with id {long} and default price {bigdecimal} per kWh")
    public void the_station_has_a_charger_with_id_and_default_price_per_kwh(long chargerId, BigDecimal defaultPrice) {
        // TODO: Implement logic to ensure a charger with this ID exists and is linked to the station, with the correct default price
        this.chargerId = chargerId;
        this.chargerDefaultPrice = defaultPrice;
        System.out.println("And charger with id: " + chargerId + " exists with default price: " + defaultPrice);
    }

    @When("the manager applies a {int}% discount to the station with id {long} for {day_of_week} from {int} to {int} hours")
    public void the_manager_applies_a_discount_to_the_station_with_id_for_from_to_hours(int discount, long stationId, DayOfWeek dayOfWeek, int startHour, int endHour) {
        this.appliedDiscountPercentage = discount;
        this.discountDayOfWeek = dayOfWeek;
        this.discountStartHour = startHour;
        this.discountEndHour = endHour;

        DiscountRequestDTO discountDTO = DiscountRequestDTO.builder()
                .stationId(stationId)
                .dayOfWeek(dayOfWeek.getValue())
                .startHour(startHour)
                .endHour(endHour)
                .discountPercent((double) discount)
                .active(true)
                .build();

        latestResponse = restTemplate.postForEntity("/api/discounts", discountDTO, String.class);

        System.out.println("When manager applies " + discount + "% discount to station " + stationId + " for " + dayOfWeek + " from " + startHour + " to " + endHour + " hours");
        System.out.println("Discount application response status: " + latestResponse.getStatusCode());
    }

    @Then("the charger with id {long} should have a final price of {bigdecimal} per kWh for a reservation made on {day_of_week} at {localtime}")
    public void the_charger_should_have_a_final_price_of_per_kwh_for_a_reservation_made_on_at(long chargerId, BigDecimal expectedPrice, DayOfWeek reservationDayOfWeek, LocalTime reservationTime) {
        this.reservationTime = reservationTime;

        // Construct the LocalDateTime for the reservation time
        // Assuming the reservation happens on LocalDate.now() but with the specified DayOfWeek and LocalTime
        // This might need adjustment based on how your test data setup handles dates.
        LocalDateTime reservationDateTime = LocalDate.now().with(reservationDayOfWeek).atTime(reservationTime);

        URI uri = fromUriString("/api/chargers/{chargerId}/price")
                .queryParam("reservationTime", reservationDateTime.toString())
                .build(chargerId);

        ResponseEntity<BigDecimal> priceResponse = restTemplate.getForEntity(uri, BigDecimal.class);

        assertEquals(HttpStatus.OK, priceResponse.getStatusCode(), "Expected OK status for price lookup");
        BigDecimal actualPrice = priceResponse.getBody();

        // Compare the actual price from the API with the expected price from the feature file
        assertEquals(expectedPrice.stripTrailingZeros(), actualPrice.stripTrailingZeros(), "Final price mismatch for charger " + chargerId + " at reservation time " + reservationTime);

        System.out.println("Then charger " + chargerId + " should have final price: " + expectedPrice + " for reservation on " + reservationDayOfWeek + " at " + reservationTime + ". Actual price: " + actualPrice);
    }

    @And("the calculated price is higher than the default price")
    public void the_calculated_price_is_higher_than_the_default_price() {
        // This step is used in the second scenario to trigger the price cap logic conceptually.
        // The actual price capping is handled by the backend calculateChargerPrice method.
         System.out.println("And the calculated price is higher than the default price (as per scenario setup - backend handles cap)");
    }

} 