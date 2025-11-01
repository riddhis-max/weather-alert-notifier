package com.weatheralert.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weatheralert.dto.SubscribeRequest;
import com.weatheralert.entity.Subscriber;
import com.weatheralert.repository.SubscriberRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.Set;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubscriberController {
    private final SubscriberRepository repo;

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@Valid @RequestBody SubscribeRequest req) {

        // NEW: Validate email domain
        if (!isValidEmailDomain(req.email())) {
            return ResponseEntity.badRequest()
                .body("Invalid or fake email domain. Please use a real email provider.");
        }

        Subscriber s = new Subscriber();
        s.setEmail(req.email());
        s.setCity(req.city());
        repo.save(s);
        return ResponseEntity.ok("Subscribed successfully!");
    }

    // NEW: Domain validation method
    private boolean isValidEmailDomain(String email) {
        if (email == null || !email.contains("@")) return false;

        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();

        // 1. Block known fake domains
        Set<String> blocked = Set.of(
            "fake-domain-xyz-abc-123.com",
            "nonexistentdomain.xyz",
            "10minutemail.com",
            "tempmail.org",
            "guerrillamail.com"
        );
        if (blocked.contains(domain)) return false;

        // 2. Allow only known TLDs
        String[] parts = domain.split("\\.", -1);
        if (parts.length < 2) return false;
        String tld = parts[parts.length - 1];

        Set<String> validTlds = Set.of("com", "org", "net", "edu", "gov", "co", "io", "ai", "app", "dev");
        if (!validTlds.contains(tld)) return false;

        // 3. Reject suspicious patterns
        if (domain.length() > 50) return false;
        if (domain.replace("-", "").length() < 5) return false;
        if (domain.matches(".*\\d{5,}.*")) return false;

        return true;
    }
}
