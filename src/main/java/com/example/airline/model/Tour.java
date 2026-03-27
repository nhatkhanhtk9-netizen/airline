package com.example.airline.model;

import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
public class Tour {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location;
    private Integer people;
    private Double price;
    private LocalDate startDate;
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Users user;

    public Tour() {
    }

    public Tour(Long id, String location, Integer people, Double price, LocalDate startDate, String description, Users user) {
        this.id = id;
        this.location = location;
        this.people = people;
        this.price = price;
        this.startDate = startDate;
        this.description = description;
        this.user = user;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getPeople() {
        return people;
    }
    public void setPeople(Integer people) {
        this.people = people;
    }

    public Double getPrice() {
        return price;
    }
    public void setPrice(Double price) {
        this.price = price;
    }

    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }



}

