package com.example.airline.controller;

import com.example.airline.model.Users;
import com.example.airline.repository.UsersRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class UsersController {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsersController(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Trang đăng ký
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new Users());
        return "register"; // register.html
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute Users user,
                               @RequestParam(name = "confirmPassword", required = false) String confirmPassword,
                               Model model) {
        if (user.getPassword() == null || confirmPassword == null || !user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu và xác nhận mật khẩu không khớp!");
            model.addAttribute("user", user);
            return "register";
        }

        String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : null;
        user.setEmail(email);

        Users existing = usersRepository.findByEmailIgnoreCase(email);
        if (existing != null) {
            if (existing.isAdmin()) {
                model.addAttribute("error", "Email đã tồn tại!");
                model.addAttribute("user", user);
                return "register";
            }
            if (existing.getPassword() == null || existing.getPassword().isBlank()) {
                existing.setFullName(user.getFullName());
                existing.setPhone(user.getPhone());
                existing.setPassword(passwordEncoder.encode(user.getPassword()));
                usersRepository.save(existing);
                return "redirect:/login";
            }
            model.addAttribute("error", "Email đã tồn tại!");
            model.addAttribute("user", user);
            return "register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        usersRepository.save(user);
        return "redirect:/login";
    }

    // Trang đăng nhập
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // login.html
    }

    @PostMapping("/api/login")
    public String loginUser(@RequestParam String email,
                            @RequestParam String password,
                            HttpSession session,
                            Model model) {

        String lowerEmail = email != null ? email.trim().toLowerCase() : "";
        Users user = usersRepository.findByEmailIgnoreCase(lowerEmail);
        if (user != null && user.getPassword() != null && passwordEncoder.matches(password, user.getPassword())) {
            session.setAttribute("loggedInUser", user);
            if (user.isAdmin()) {
                return "redirect:/admin";
            }
            return "redirect:/index1";
        } else {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng!");
            return "login";
        }
    }

    // Đăng xuất
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        Users user = (Users) session.getAttribute("loggedInUser");
        boolean wasAdmin = (user != null && user.isAdmin());
        session.invalidate();
        return wasAdmin ? "redirect:/admin/login" : "redirect:/login";
    }
}
