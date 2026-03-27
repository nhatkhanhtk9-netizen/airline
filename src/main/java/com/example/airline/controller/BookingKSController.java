package com.example.airline.controller;

import com.example.airline.model.BookingKS;
import com.example.airline.model.Users;
import com.example.airline.repository.BookingKSRepository;
import com.example.airline.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/bookingKS")
public class BookingKSController {

    @Autowired
    private BookingKSRepository bookingKSRepository;

    @Autowired
    private UsersRepository usersRepository;

    // Hiển thị form
    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("bookingKS", new BookingKS());
        return "bookingKSForm"; // Tên file HTML
    }

    // Xử lý submit form và hiển thị kết quả tìm kiếm
    @PostMapping("/submit")
    public String submitForm(@ModelAttribute BookingKS bookingKS, Model model) {
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
