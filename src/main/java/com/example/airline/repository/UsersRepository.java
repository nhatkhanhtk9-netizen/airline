package com.example.airline.repository;

import com.example.airline.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Users findByEmailIgnoreCase(String email);
    Users findByEmailAndPassword(String email, String password);

    // return all users who are administrators
    List<Users> findByAdminTrue();

    List<Users> findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrPhoneContainingIgnoreCase(
            String email,
            String fullName,
            String phone
    );
}

