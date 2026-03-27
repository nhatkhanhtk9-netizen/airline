package com.example.airline.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestRestController {

    @GetMapping("/test")
    public String test() {
        return "API OK!";
    }
}
