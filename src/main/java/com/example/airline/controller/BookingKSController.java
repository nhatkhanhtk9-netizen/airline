package com.example.airline.controller;

import com.example.airline.model.BookingKS;
import com.example.airline.model.Users;
import com.example.airline.repository.BookingKSRepository;
import com.example.airline.repository.UsersRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class BookingKSController {

    @Autowired
    private BookingKSRepository bookingKSRepository;

    @Autowired
    private UsersRepository usersRepository;

    // Hiển thị form qua đường dẫn /hotels hoặc /bookingKS/form
    @GetMapping({"/hotels", "/bookingKS/form"})
    public String showForm(HttpSession session, Model model) {
        Users loggedInUser = (Users) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        
        BookingKS bookingKS = new BookingKS();
        bookingKS.setFullName(loggedInUser.getFullName());
        bookingKS.setEmail(loggedInUser.getEmail());
        bookingKS.setPhone(loggedInUser.getPhone());
        
        model.addAttribute("bookingKS", bookingKS);
        return "bookingKSForm"; // Tên file HTML
    }

    // Xử lý submit form và hiển thị kết quả tìm kiếm
    @PostMapping("/bookingKS/submit")
    public String submitForm(@ModelAttribute BookingKS bookingKS, HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        bookingKSRepository.save(bookingKS);
        if (bookingKS.getEmail() != null && !bookingKS.getEmail().isBlank()) {
            String cleanEmail = bookingKS.getEmail().trim().toLowerCase();
            Users existing = usersRepository.findByEmailIgnoreCase(cleanEmail);
            if (existing == null) {
                Users u = new Users();
                u.setFullName(bookingKS.getFullName());
                u.setEmail(cleanEmail);
                u.setPhone(bookingKS.getPhone());
                u.setAdmin(false);
                u.setPassword(null);
                usersRepository.save(u);
            } else {
                if ((existing.getFullName() == null || existing.getFullName().isBlank()) && bookingKS.getFullName() != null) {
                    existing.setFullName(bookingKS.getFullName());
                }
                if ((existing.getPhone() == null || existing.getPhone().isBlank()) && bookingKS.getPhone() != null) {
                    existing.setPhone(bookingKS.getPhone());
                }
                usersRepository.save(existing);
            }
        }
        model.addAttribute("booking", bookingKS);
        return "hotelResults"; // Chuyển sang trang kết quả tìm kiếm
    }
}
