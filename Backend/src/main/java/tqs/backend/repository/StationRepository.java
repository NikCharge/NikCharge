// src/main/java/tqs/backend/repository/StationRepository.java
package tqs.backend.repository;

import tqs.backend.model.Station;

import java.util.List;

public interface StationRepository {
    List<Station> findAll();
}
