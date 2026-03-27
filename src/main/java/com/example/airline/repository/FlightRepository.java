package com.example.airline.repository;

import com.example.airline.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByOriginContainingIgnoreCaseOrDestinationContainingIgnoreCase(String origin, String destination);

    List<Flight> findByOriginIgnoreCaseAndDestinationIgnoreCaseAndDepartureTimeBetweenOrderByDepartureTimeAsc(
            String origin,
            String destination,
            LocalDateTime start,
            LocalDateTime end
    );
}
