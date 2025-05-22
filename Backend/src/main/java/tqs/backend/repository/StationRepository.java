// src/main/java/tqs/backend/repository/StationRepository.java
package tqs.backend.repository;

import tqs.backend.model.Station;

import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;


@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    List<Station> findAll();
}
