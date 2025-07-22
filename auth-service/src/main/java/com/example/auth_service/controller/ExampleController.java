package com.example.auth_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ExampleController {

    @GetMapping("/guest/hello")
    @PreAuthorize("hasAnyRole('GUEST', 'PREMIUM_USER', 'ADMIN')")
    public ResponseEntity<String> helloGuest() {
        return ResponseEntity.ok("Hello from a Guest endpoint!");
    }

    @GetMapping("/premium/hello")
    @PreAuthorize("hasAnyRole('PREMIUM_USER', 'ADMIN')")
    public ResponseEntity<String> helloPremium() {
        return ResponseEntity.ok("Hello from a Premium endpoint!");
    }

    @GetMapping("/admin/hello")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> helloAdmin() {
        return ResponseEntity.ok("Hello from an Admin endpoint!");
    }
}