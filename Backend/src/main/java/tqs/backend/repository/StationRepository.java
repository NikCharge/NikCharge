package tqs.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import tqs.backend.model.Station;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {

    // Buscar estação com carregadores já carregados para evitar N+1
    @EntityGraph(attributePaths = {"chargers"})
    Optional<Station> findById(Long id);

    List<Station> findByCity(String city);
}
 