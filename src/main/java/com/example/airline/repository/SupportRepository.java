package com.example.airline.repository;

import com.example.airline.model.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportRepository extends JpaRepository<SupportMessage, Long> {
    List<SupportMessage> findByEmailOrderByCreatedAtDesc(String email);
}
