package tqs.backend.stepdefs;

import io.cucumber.java.en.And;
import org.springframework.beans.factory.annotation.Autowired;
import tqs.backend.model.*;
import tqs.backend.model.enums.ChargerStatus;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.util.CommonReservationHelper;

public class StationStepDefs {

    @Autowired
    private CommonReservationHelper helper;

    public static Station currentStation;
    public static Charger currentCharger;

    @And("a station {string} exists with an available charger")
    public void create_station_with_available_charger(String stationName) {
        currentStation = helper.createStation(stationName, "Main Ave", "City");
        currentCharger = helper.createCharger(currentStation, ChargerType.DC_FAST, ChargerStatus.AVAILABLE);
    }
}
