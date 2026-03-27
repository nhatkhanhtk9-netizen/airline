package com.example.airline.controller;

import com.example.airline.model.*;

import com.example.airline.repository.TourRepository;
import com.example.airline.repository.UsersRepository;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tour")
public class TourController {

    private final TourRepository tourRepository;
    private final UsersRepository usersRepository;

    public TourController(TourRepository tourRepository, UsersRepository usersRepository) {
        this.tourRepository = tourRepository;
        this.usersRepository = usersRepository;
    }

    // Lấy danh sách tour
    @GetMapping
    public List<Tour> getAllTours() {   
        return tourRepository.findAll();
    }

    // Tạo tour mới
    @PostMapping
    public ResponseEntity<Tour> createTour(@RequestBody Tour tour) {
        // Kiểm tra user
        if (tour.getUser() != null) {
            Optional<Users> userOpt = usersRepository.findById(Long.valueOf(tour.getUser().getId()));
            userOpt.ifPresent(tour::setUser);
        }
        Tour savedTour = tourRepository.save(tour);
        return ResponseEntity.ok(savedTour);
    }

    // Cập nhật tour
    @PutMapping("/{id}")
    public ResponseEntity<Tour> updateTour(@PathVariable Long id, @RequestBody Tour tour) {
        Tour existingTour = tourRepository.findById(id).orElseThrow(() -> new RuntimeException("Tour not found"));
        existingTour.setLocation(tour.getLocation());
        existingTour.setPeople(tour.getPeople());
        existingTour.setPrice(tour.getPrice());
        existingTour.setStartDate(tour.getStartDate());
        existingTour.setDescription(tour.getDescription());
        if (tour.getUser() != null) {
            Optional<Users> userOpt = usersRepository.findById(Long.valueOf(tour.getUser().getId()));
            userOpt.ifPresent(existingTour::setUser);
        }
        return ResponseEntity.ok(tourRepository.save(existingTour));
    }

    // Xóa tour
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTour(@PathVariable Long id) {
        tourRepository.deleteById(id);
        return ResponseEntity.ok("Tour deleted successfully");
    }
}
