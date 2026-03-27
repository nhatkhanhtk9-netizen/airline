package com.example.airline.controller;

import com.example.airline.model.Booking;
import com.example.airline.model.BookingKS;
import com.example.airline.model.FlightBooking;
import com.example.airline.model.Users;
import com.example.airline.repository.BookingKSRepository;
import com.example.airline.repository.BookingRepository;
import com.example.airline.repository.FlightBookingRepository;
import com.example.airline.repository.FlightRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class GeneralController {

    private final FlightRepository flightRepository;
    private final FlightBookingRepository flightBookingRepository;
    private final BookingRepository bookingRepository;
    private final BookingKSRepository bookingKSRepository;

    public GeneralController(FlightRepository flightRepository, 
                             FlightBookingRepository flightBookingRepository,
                             BookingRepository bookingRepository,
                             BookingKSRepository bookingKSRepository) {
        this.flightRepository = flightRepository;
        this.flightBookingRepository = flightBookingRepository;
        this.bookingRepository = bookingRepository;
        this.bookingKSRepository = bookingKSRepository;
    }

    @GetMapping("/promotions")
    public String promotions() {
        return "index";
    }

    @GetMapping("/index1")
    public String index1(Model model) {
        model.addAttribute("flights", flightRepository.findAll());
        return "index1";
    }

    @GetMapping("/my-booking")
    public String myBooking(HttpSession session, Model model) {
        Users loggedInUser = (Users) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        String email = loggedInUser.getEmail() != null ? loggedInUser.getEmail().trim() : "";
        System.out.println("DEBUG: Looking for bookings for email: " + email);
        
        List<FlightBooking> flightBookings = flightBookingRepository.findByEmailIgnoreCase(email);
        List<Booking> generalBookings = bookingRepository.findByEmailIgnoreCase(email);
        List<BookingKS> hotelBookings = bookingKSRepository.findByEmailIgnoreCase(email);

        model.addAttribute("flightBookings", flightBookings);
        model.addAttribute("generalBookings", generalBookings);
        model.addAttribute("hotelBookings", hotelBookings);
        model.addAttribute("currentSearchEmail", email);
        
        List<FlightBooking> allData = flightBookingRepository.findAll();
        model.addAttribute("allDataForDebug", allData);
        
        return "booking";
    }

    @GetMapping("/my-bookings")
    public String myBookingsRedirect() {
        return "redirect:/my-booking";
    }

    @GetMapping("/trains")
    public String trains() {
        return "index";
    }

    @GetMapping("/buses")
    public String buses() {
        return "index";
    }

    @GetMapping("/activities")
    public String activities() {
        return "index";
    }

    @GetMapping("/car-rental")
    public String carRental() {
        return "index";
    }

    @GetMapping("/partnership")
    public String partnership() {
        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam(name = "query", required = false) String query) {
        return "redirect:/flights?search=" + query;
    }
}