package com.example.airline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.airline.model.Tour;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
}
