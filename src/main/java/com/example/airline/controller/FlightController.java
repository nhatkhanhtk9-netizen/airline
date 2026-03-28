package com.example.airline.controller;

import com.example.airline.model.Flight;
import com.example.airline.model.FlightSearch;
import com.example.airline.model.Users;
import com.example.airline.repository.FlightRepository;
import com.example.airline.repository.FlightSearchRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FlightController {
    private final FlightRepository flightRepository;
    private final FlightSearchRepository flightSearchRepository;

    private static final Map<String, String> AIRPORT_CODE_BY_CITY = Map.of(
            "Hà Nội", "HAN",
            "TP. Hồ Chí Minh", "SGN",
            "Đà Nẵng", "DAD",
            "Nha Trang", "CXR",
            "Phú Quốc", "PQC",
            "Hải Phòng", "HPH",
            "Cần Thơ", "VCA",
            "Huế", "HUI"
    );

    private static final Map<String, String> AIRPORT_NAME_BY_CITY = Map.of(
            "Hà Nội", "Nội Bài",
            "TP. Hồ Chí Minh", "Tân Sơn Nhất",
            "Đà Nẵng", "Đà Nẵng",
            "Nha Trang", "Cam Ranh",
            "Phú Quốc", "Phú Quốc",
            "Hải Phòng", "Cát Bi",
            "Cần Thơ", "Cần Thơ",
            "Huế", "Phú Bài"
    );

    public FlightController(FlightRepository flightRepository, FlightSearchRepository flightSearchRepository) {
        this.flightRepository = flightRepository;
        this.flightSearchRepository = flightSearchRepository;

    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/index1";
        }
        List<Flight> flights = flightRepository.findAll();
        model.addAttribute("flights", flights);
        return "index"; // Thymeleaf template: src/main/resources/templates/index.html
    }

    // Trang danh sách tất cả chuyến bay
    @GetMapping("/flights")
    public String listFlights(
            @RequestParam(name = "origin", required = false) String origin,
            @RequestParam(name = "destination", required = false) String destination,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "returnDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
            @RequestParam(name = "guests", required = false) Integer guests,
            @RequestParam(name = "flyTrip", required = false) String flyTrip,
            @RequestParam(name = "submitted", required = false) String submitted,
            @RequestParam(name = "search", required = false) String search,
            HttpSession session,
            Model model
    ) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        List<Flight> flights;
        boolean searchActive = false;

        if (origin != null && !origin.isBlank() && destination != null && !destination.isBlank() && date != null) {
            searchActive = true;
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);
            flights = flightRepository
                    .findByOriginIgnoreCaseAndDestinationIgnoreCaseAndDepartureTimeBetweenOrderByDepartureTimeAsc(
                            origin.trim(),
                            destination.trim(),
                            start,
                            end
                    );
            model.addAttribute("searchOrigin", origin.trim());
            model.addAttribute("searchDestination", destination.trim());
            model.addAttribute("searchDate", date);

            if ("1".equals(submitted)) {
                Object o = session.getAttribute("loggedInUser");
                Users user = (o instanceof Users) ? (Users) o : null;
                String sessionId = session.getId();
                String signature = (origin.trim() + "|" + destination.trim() + "|" + date + "|" + flyTrip + "|" + returnDate + "|" + guests);
                Object lastSig = session.getAttribute("lastFlightSearchSignature");
                if (lastSig == null || !signature.equals(lastSig.toString())) {
                    FlightSearch fs = new FlightSearch();
                    fs.setSessionId(sessionId);
                    fs.setUser(user);
                    fs.setOrigin(origin.trim());
                    fs.setDestination(destination.trim());
                    fs.setDepartureDate(date);
                    fs.setReturnDate(returnDate);
                    fs.setGuests(guests);
                    fs.setTripType(flyTrip);
                    fs.setCreatedAt(LocalDateTime.now());
                    flightSearchRepository.save(fs);
                    session.setAttribute("lastFlightSearchSignature", signature);
                }
            }
        } else if (search != null && !search.isBlank()) {
            searchActive = true;
            flights = flightRepository.findByOriginContainingIgnoreCaseOrDestinationContainingIgnoreCase(search.trim(), search.trim());
            model.addAttribute("search", search.trim());
        } else {
            flights = flightRepository.findAll();
        }

        model.addAttribute("flights", flights);
        model.addAttribute("searchActive", searchActive);
        model.addAttribute("airportCodeByCity", AIRPORT_CODE_BY_CITY);
        model.addAttribute("airportNameByCity", AIRPORT_NAME_BY_CITY);

        Map<Long, String> airlineByFlightId = new HashMap<>();
        for (Flight f : flights) {
            String name = (f.getAirline() != null && !f.getAirline().isBlank())
                    ? f.getAirline()
                    : airlineNameFromFlightNumber(f.getFlightNumber());
            airlineByFlightId.put(f.getId(), name);
            
            // Tự động gán logo nếu chưa có trong DB
            if (f.getAirlineLogo() == null || f.getAirlineLogo().isBlank()) {
                f.setAirlineLogo(airlineLogoFromFlightNumber(f.getFlightNumber()));
            }
        }
        model.addAttribute("airlineByFlightId", airlineByFlightId);
        return "flights"; // flights.html
    }

    // Trang chi tiết chuyến bay
    @GetMapping("/flights/{id}")
    public String flightDetail(@PathVariable Long id, Model model) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found"));
        model.addAttribute("flight", flight); // singular
        model.addAttribute("airportCodeByCity", AIRPORT_CODE_BY_CITY);
        model.addAttribute("airportNameByCity", AIRPORT_NAME_BY_CITY);
        model.addAttribute("airlineName", airlineNameFromFlightNumber(flight.getFlightNumber()));
        return "flight-detail"; // flight-detail.html
    }

    private static String airlineNameFromFlightNumber(String flightNumber) {
        if (flightNumber == null || flightNumber.length() < 2) {
            return "Hãng bay";
        }
        String code = flightNumber.substring(0, 2).toUpperCase();
        return switch (code) {
            case "VN" -> "Vietnam Airlines";
            case "VJ" -> "VietJet Air";
            case "QH" -> "Bamboo Airways";
            case "BL" -> "Pacific Airlines";
            default -> "Hãng bay " + code;
        };
    }

    private static String airlineLogoFromFlightNumber(String flightNumber) {
        if (flightNumber == null || flightNumber.length() < 2) {
            return null;
        }
        String code = flightNumber.substring(0, 2).toUpperCase();
        return switch (code) {
            case "VN" -> "vna.png";
            case "VJ" -> "vietjet.png";
            case "QH" -> "bamboo.png";
            case "BL" -> "pacific.png";
            default -> null;
        };
    }
}
