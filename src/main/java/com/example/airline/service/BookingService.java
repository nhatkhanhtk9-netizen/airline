package com.example.airline.service;

import com.example.airline.model.Flight;
import com.example.airline.model.Ticket;
import com.example.airline.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.airline.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BookingService {
    private final FlightRepository flightRepo;
    private final TicketRepository ticketRepo;


    
    public BookingService(FlightRepository flightRepo, TicketRepository ticketRepo) {
        this.flightRepo = flightRepo;
        this.ticketRepo = ticketRepo;
    }

    @Transactional
    public Ticket bookTicket(Long flightId, String passengerName, String passengerEmail, int seats) {
        Flight flight = flightRepo.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found"));
        if (flight.getSeatsAvailable() < seats) {
            throw new RuntimeException("Không đủ chỗ trống");
        }
        flight.setSeatsAvailable(flight.getSeatsAvailable() - seats);
        flightRepo.save(flight);

        Ticket t = new Ticket();
        t.setFlight(flight);
        t.setPassengerName(passengerName);
        t.setPassengerEmail(passengerEmail);
        t.setSeatsBooked(seats);
        return ticketRepo.save(t);
    }
}
