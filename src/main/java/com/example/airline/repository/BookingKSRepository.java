package com.example.airline.repository;


import com.example.airline.model.BookingKS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingKSRepository extends JpaRepository<BookingKS, Long> {
    List<BookingKS> findByEmailIgnoreCase(String email);
}

