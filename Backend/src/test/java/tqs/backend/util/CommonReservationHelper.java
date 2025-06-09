package tqs.backend.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tqs.backend.model.*;
import tqs.backend.model.enums.*;
import tqs.backend.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class CommonReservationHelper {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ChargerRepository chargerRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    public Client createClient(String name, String email, String password) {
        Client client = Client.builder()
                .name(name)
                .email(email)
                .passwordHash(password)
                .batteryCapacityKwh(50.0)
                .fullRangeKm(300.0)
                .build();
        return clientRepository.save(client);
    }

    public Station createStation(String name, String address, String city) {
        double lat = 39.0 + Math.random();
        double lon = -8.0 + Math.random();

        Station station = Station.builder()
                .name(name)
                .address(address)
                .city(city)
                .latitude(lat)
                .longitude(lon)
                .build();
        return stationRepository.save(station);
    }

    public Charger createCharger(Station station, ChargerType type, ChargerStatus status) {
        Charger charger = Charger.builder()
                .station(station)
                .chargerType(type)
                .status(status)
                .pricePerKwh(BigDecimal.valueOf(0.30))
                .build();
        return chargerRepository.save(charger);
    }

    public Reservation createReservation(Client client, Charger charger, ReservationStatus status,
                                         LocalDateTime startTime, LocalDateTime endTime) {
        Reservation reservation = Reservation.builder()
                .user(client)
                .charger(charger)
                .startTime(startTime)
                .estimatedEndTime(endTime)
                .batteryLevelStart(20.0)
                .estimatedKwh(30.0)
                .estimatedCost(new BigDecimal("15.00"))
                .status(status)
                .build();
        return reservationRepository.save(reservation);
    }

    public void clearAllClients() {
        clientRepository.deleteAll();
    }
}