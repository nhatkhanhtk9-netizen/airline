package com.example.airline.repository;

import com.example.airline.model.FlightBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlightBookingRepository extends JpaRepository<FlightBooking, Long> {
    boolean existsByBookingCode(String bookingCode);
    List<FlightBooking> findByEmailIgnoreCase(String email);
}
