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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

        // Chuyển hướng trực tiếp đến trang đặt lại mật khẩu với token vừa tạo
        return "redirect:/reset-password?token=" + token;
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

    // --- TRANG CÁ NHÂN ---

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Users loggedInUser = (Users) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        
        // Reload user from database to ensure fresh data
        Users user = usersRepository.findById(loggedInUser.getId()).orElse(null);
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam String phone,
                                @RequestParam(required = false) String newPassword,
                                @RequestParam(required = false) String confirmPassword,
                                @RequestParam(value = "profilePic", required = false) MultipartFile profilePic,
                                HttpSession session,
                                Model model) {
        Users loggedInUser = (Users) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Users user = usersRepository.findById(loggedInUser.getId()).orElseThrow();
        user.setFullName(fullName);
        user.setPhone(phone);

        // Xử lý Upload Ảnh đại diện
        if (profilePic != null && !profilePic.isEmpty()) {
            try {
                // Đường dẫn lưu file: src/main/resources/static/images/avatars/
                String uploadDir = "src/main/resources/static/images/avatars/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Tên file duy nhất
                String fileName = UUID.randomUUID().toString() + "_" + profilePic.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);

                Files.copy(profilePic.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                
                // Lưu tên file vào database
                user.setProfileImage(fileName);
                System.out.println(">>> Profile image uploaded: " + fileName);

            } catch (IOException e) {
                System.err.println(">>> Error uploading profile image: " + e.getMessage());
                model.addAttribute("error", "Lỗi khi tải lên ảnh: " + e.getMessage());
            }
        }

        if (newPassword != null && !newPassword.isBlank()) {
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("user", user);
                model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
                return "profile";
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        usersRepository.save(user);
        session.setAttribute("loggedInUser", user); // Update session data
        
        model.addAttribute("user", user);
        model.addAttribute("message", "Cập nhật thông tin thành công!");
        return "profile";
    }
}
