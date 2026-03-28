package com.example.airline.controller;

import com.example.airline.model.Users;
import com.example.airline.repository.UsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

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
        System.out.println(">>> Login Attempt: " + lowerEmail);
        
        Users user = usersRepository.findByEmailIgnoreCase(lowerEmail);
        if (user == null) {
            System.out.println(">>> Login Failed: User not found for email " + lowerEmail);
            model.addAttribute("error", "Email không tồn tại!");
            return "login";
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            System.out.println(">>> Login Success: " + lowerEmail + (user.isAdmin() ? " (Admin)" : " (User)"));
            session.setAttribute("loggedInUser", user);
            if (user.isAdmin()) {
                return "redirect:/admin";
            }
            return "redirect:/index1";
        } else {
            System.out.println(">>> Login Failed: Wrong password for " + lowerEmail);
            model.addAttribute("error", "Mật khẩu không chính xác!");
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

    // --- QUÊN MẬT KHẨU ---

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, HttpServletRequest request, Model model) {
        Users user = usersRepository.findByEmailIgnoreCase(email.trim().toLowerCase());
        
        if (user == null) {
            model.addAttribute("error", "Email không tồn tại trong hệ thống!");
            return "forgot-password";
        }

        // Tạo token ngẫu nhiên
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1)); // Token có hiệu lực trong 1 giờ
        usersRepository.save(user);

        // Lấy domain hiện tại (local hoặc railway)
        String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), "");
        String resetLink = baseUrl + "/reset-password?token=" + token;
        System.out.println(">>> RESET PASSWORD LINK: " + resetLink);

        model.addAttribute("message", "Chúng tôi đã gửi link đặt lại mật khẩu vào email của bạn. (Vui lòng kiểm tra Console/Log để lấy link)");
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        Users user = usersRepository.findByResetToken(token);
        
        if (user == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Link đã hết hạn hoặc không hợp lệ!");
            return "forgot-password";
        }

        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,
                                       @RequestParam String password,
                                       @RequestParam String confirmPassword,
                                       Model model) {
        Users user = usersRepository.findByResetToken(token);
        
        if (user == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Link đã hết hạn hoặc không hợp lệ!");
            return "forgot-password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            model.addAttribute("token", token);
            return "reset-password";
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        usersRepository.save(user);

        model.addAttribute("error", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
        return "login";
    }
}
