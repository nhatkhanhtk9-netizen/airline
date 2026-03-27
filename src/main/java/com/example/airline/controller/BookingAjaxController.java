package com.example.airline.controller;

import com.example.airline.model.Booking;
import com.example.airline.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/booking")
public class BookingAjaxController {

    @Autowired
    private BookingRepository bookingRepository;

    // Hiển thị form đặt vé
    @GetMapping
    public String bookingForm(Model model) {
        model.addAttribute("booking", new Booking());
        return "booking";
    }

    // Xử lý submit form, chuyển sang trang QR thanh toán
   @PostMapping("/submit")
public String submitBooking(@ModelAttribute Booking booking, Model model) {
    if (booking.getPassengers() <= 0) booking.setPassengers(1);
    if (booking.getPricePerTicket() == null || booking.getPricePerTicket() < 0)
        booking.setPricePerTicket(0L);

    booking.setTotalPrice(booking.getPricePerTicket() * booking.getPassengers());
    bookingRepository.save(booking);

    model.addAttribute("booking", booking);

    // Tạo URL thanh toán QR (ví dụ giả lập)
    String qrData = "https://payment.example.com/pay?amount=" + booking.getTotalPrice()
            + "&bookingId=" + booking.getId();
    model.addAttribute("qrData", qrData);

    return "bookingQR"; // chuyển sang trang QR
}


    // AJAX cập nhật trực tiếp
    @PostMapping("/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateBookingAjax(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates
    ) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return ResponseEntity.notFound().build();

        try {
            if (updates.containsKey("passengers")) {
                booking.setPassengers(((Number) updates.get("passengers")).intValue());
            }
            if (updates.containsKey("pricePerTicket")) {
                booking.setPricePerTicket(((Number) updates.get("pricePerTicket")).longValue());
            }

            // Tính lại tổng tiền trên server
            booking.setTotalPrice(booking.getPassengers() * booking.getPricePerTicket());

            bookingRepository.save(booking);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi khi cập nhật booking: " + e.getMessage());
        }
    }
}
