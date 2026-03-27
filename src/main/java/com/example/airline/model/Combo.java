package com.example.airline.model;

import jakarta.persistence.*;       // Cho @Entity, @Id, @GeneratedValue, @Column
import jakarta.validation.constraints.*; // Nếu muốn thêm validation

@Entity
@Table(name = "combo")
public class Combo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên combo không được để trống")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Giá combo không được để trống")
    private Double price;

    @Column(length = 1000)
    private String description;
    
    public Combo() {
    }

    public Combo(Long id, String name, Double price, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
