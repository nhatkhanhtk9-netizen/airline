package com.example.airline.controller;

import com.example.airline.model.Flight;
import com.example.airline.model.FlightBooking;
import com.example.airline.model.Users;
import com.example.airline.repository.FlightRepository;
import com.example.airline.repository.FlightBookingRepository;
import com.example.airline.repository.UsersRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.List;

@Controller
@RequestMapping("/flight")
public class FlightBookingController {

    @Autowired
    private FlightBookingRepository flightBookingRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/form")
    public String showForm(@RequestParam(required = false) Long flightId, HttpSession session, Model model) {
        Users loggedInUser = (Users) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if (flightId != null) {
            flightRepository.findById(flightId).ifPresent(flight -> {
                model.addAttribute("prefillFrom", flight.getOrigin());
                model.addAttribute("prefillTo", flight.getDestination());
                model.addAttribute("prefillDepartureDate", flight.getDepartureTime().toLocalDate().toString());
            });
        }
        
        model.addAttribute("prefillFullName", loggedInUser.getFullName());
        model.addAttribute("prefillEmail", loggedInUser.getEmail());
        model.addAttribute("prefillPhone", loggedInUser.getPhone());
        
        return "flightBookingForm";
    }

    @PostMapping("/submit")
    public String submitFlightBooking(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam String departureDate,
            @RequestParam(required = false) String returnDate,
            @RequestParam int passenger,
            @RequestParam String classType,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String idCard,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        System.out.println(">>> [CONTROLLER] Received submission from: " + fullName + " (" + email + ")");
        
        try {
            FlightBooking booking = new FlightBooking();
            booking.setFromLocation(from);
            booking.setToLocation(to);
            booking.setDepartureDate(departureDate);
            booking.setReturnDate((returnDate == null || returnDate.isBlank()) ? null : returnDate);
            booking.setPassenger(passenger);
            booking.setClassType(classType);
            booking.setFullName(fullName);
            
            String cleanEmail = (email != null) ? email.trim() : "";
            booking.setEmail(cleanEmail);
            booking.setPhone(phone);
            booking.setIdCard(idCard);
            booking.setStatus("PENDING");
            
            double basePrice = 1500000.0;
            booking.setTotalPrice(basePrice * passenger);

            // Save the booking
            System.out.println(">>> [DB] Saving booking to database...");
            FlightBooking savedBooking = flightBookingRepository.save(booking);
            System.out.println(">>> [DB] Saved success! ID: " + savedBooking.getId());

            // Generate booking code
            String code = String.format("FB%06d", savedBooking.getId());
            savedBooking.setBookingCode(code);
            flightBookingRepository.save(savedBooking);
            System.out.println(">>> [DB] Code generated and updated: " + code);

            // Sync User
            if (!cleanEmail.isEmpty()) {
                Users existing = usersRepository.findByEmailIgnoreCase(cleanEmail);
                if (existing == null) {
                    Users u = new Users();
                    u.setFullName(fullName);
                    u.setEmail(cleanEmail);
                    u.setPhone(phone);
                    u.setAdmin(false);
                    usersRepository.save(u);
                    System.out.println(">>> [DB] New user created for email: " + cleanEmail);
                }
            }

            System.out.println(">>> [FLOW] Redirecting to confirm page for ID: " + savedBooking.getId());
            return "redirect:/flight/confirm/" + savedBooking.getId();

        } catch (Exception e) {
            System.err.println(">>> [ERROR] Submission failed: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/flight/form";
        }
    }

    /* 
    @PostMapping("/quick-book/{id}")
    public String quickBook(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Users loggedInUser = (Users) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        try {
            Flight flight = flightRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến bay"));

            FlightBooking booking = new FlightBooking();
            booking.setFromLocation(flight.getOrigin());
            booking.setToLocation(flight.getDestination());
            booking.setDepartureDate(flight.getDepartureTime().toLocalDate().toString());
            booking.setPassenger(1); // Mặc định 1 khách cho đặt nhanh
            booking.setClassType("Phổ thông");
            booking.setFullName(loggedInUser.getFullName());
            booking.setEmail(loggedInUser.getEmail());
            booking.setPhone(loggedInUser.getPhone());
            booking.setIdCard("Chưa cập nhật");
            booking.setStatus("PENDING");
            booking.setTotalPrice(flight.getPrice());

            FlightBooking savedBooking = flightBookingRepository.save(booking);
            String code = String.format("FB%06d", savedBooking.getId());
            savedBooking.setBookingCode(code);
            flightBookingRepository.save(savedBooking);

            return "redirect:/flight/confirm/" + savedBooking.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi đặt vé nhanh: " + e.getMessage());
            return "redirect:/flights";
        }
    }
    */

    @GetMapping("/confirm/{id}")
    public String showConfirmPage(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        FlightBooking booking = flightBookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt vé"));
        model.addAttribute("booking", booking);
        return "confirm-booking";
    }

    @PostMapping("/payment/{id}")
    public String showPayment(
            @PathVariable Long id, 
            @RequestParam(required = false, defaultValue = "false") boolean insurance,
            @RequestParam(required = false, defaultValue = "0") int baggageWeight,
            @RequestParam(required = false) String voucherCode,
            HttpSession session,
            Model model
    ) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        FlightBooking booking = flightBookingRepository.findById(id).orElseThrow();
        booking.setInsurance(insurance);
        booking.setBaggageWeight(baggageWeight);
        booking.setVoucherCode(voucherCode);

        double additionalPrice = 0.0;
        if (insurance) {
            additionalPrice += 200000.0 * booking.getPassenger();
        }
        if (baggageWeight > 0) {
            additionalPrice += (baggageWeight / 5) * 100000.0;
        }

        double discount = 0.0;
        if (voucherCode != null && !voucherCode.isBlank()) {
            String code = voucherCode.trim().toUpperCase();
            if (code.equals("FLYSAFE")) {
                discount = 100000.0;
            } else if (code.equals("PROMO2026")) {
                discount = booking.getTotalPrice() * 0.1;
            }
        }
        booking.setDiscountAmount(discount);

        double finalPrice = booking.getTotalPrice() + additionalPrice - discount;
        booking.setTotalPrice(finalPrice);

        int points = (int) (finalPrice / 1000);
        booking.setPointsEarned(points);

        booking.setStatus("CONFIRMED");
        flightBookingRepository.save(booking);
        
        model.addAttribute("booking", booking);
        return "payment";
    }

    // Xử lý thanh toán thành công
    @PostMapping("/pay-request/{id}")
    public String payRequest(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        FlightBooking booking = flightBookingRepository.findById(id).orElseThrow();
        booking.setStatus("PAYMENT_PENDING");
        flightBookingRepository.save(booking);
        
        model.addAttribute("booking", booking);
        return "flightBookingSuccess";
    }

    @GetMapping("/status/{id}")
    @ResponseBody
    public ResponseEntity<?> getBookingStatus(@PathVariable Long id) {
        return flightBookingRepository.findById(id)
                .map(booking -> ResponseEntity.ok(Map.of("status", booking.getStatus())))
                .orElse(ResponseEntity.notFound().build());
    }
}
