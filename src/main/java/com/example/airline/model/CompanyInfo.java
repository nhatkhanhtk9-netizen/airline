package com.example.airline.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CompanyInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String type; // e.g., "about", "news", "careers", "terms", "privacy"

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    public CompanyInfo() {}

    public CompanyInfo(String type, String title, String content) {
        this.type = type;
        this.title = title;
        this.content = content;
    }
}
