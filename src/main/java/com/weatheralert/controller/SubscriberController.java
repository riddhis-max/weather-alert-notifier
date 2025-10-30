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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubscriberController {
    private final SubscriberRepository repo;

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@Valid @RequestBody SubscribeRequest req) {
        Subscriber s = new Subscriber();
        s.setEmail(req.email());
        s.setCity(req.city());
        repo.save(s);
        return ResponseEntity.ok("Subscribed successfully!");
    }
}
