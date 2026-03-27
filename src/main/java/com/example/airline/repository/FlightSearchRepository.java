package com.example.airline.repository;

import com.example.airline.model.FlightSearch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightSearchRepository extends JpaRepository<FlightSearch, Long> {
}
