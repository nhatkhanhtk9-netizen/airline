package com.example.airline.model;

import jakarta.persistence.*;

@Entity
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

    // Manual Getter/Setter because Lombok is missing
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
