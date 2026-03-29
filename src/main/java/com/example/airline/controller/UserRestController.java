package com.example.airline.controller;

import com.example.airline.model.Users;
import com.example.airline.repository.UsersRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRestController(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Lấy danh sách tất cả người dùng
    @GetMapping
    public List<Users> getAllUsers() {
        return usersRepository.findAll();
    }

    // Lấy thông tin 1 người dùng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Users> getUserById(@PathVariable Long id) {
        return usersRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Tạo người dùng mới
    @PostMapping
    public Users createUser(@RequestBody Users user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return usersRepository.save(user);
    }

    // Cập nhật người dùng
    @PutMapping("/{id}")
    public ResponseEntity<Users> updateUser(@PathVariable Long id, @RequestBody Users userDetails) {
        return usersRepository.findById(id).map(user -> {
            user.setFullName(userDetails.getFullName());
            user.setPhone(userDetails.getPhone());
            if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }
            return ResponseEntity.ok(usersRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Xóa người dùng
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (usersRepository.existsById(id)) {
            usersRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
