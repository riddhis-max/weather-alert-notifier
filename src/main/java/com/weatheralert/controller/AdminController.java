package com.weatheralert.controller;

import com.weatheralert.entity.Subscriber;
import com.weatheralert.repository.SubscriberRepository;
import com.weatheralert.scheduler.AlertScheduler;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final SubscriberRepository subscriberRepository;
    private final AlertScheduler alertScheduler;

    @GetMapping
    public String adminPage(Model model, HttpServletResponse response) {
        List<Subscriber> rawSubs = subscriberRepository.findAll();
    
        // NEW: Transform list to display "N/A" for empty city
        List<Subscriber> displaySubs = rawSubs.stream()
            .map(sub -> {
                Subscriber copy = new Subscriber();
                copy.setId(sub.getId());
                copy.setEmail(maskEmail(sub.getEmail()));
                copy.setCity(
                    sub.getCity() == null || sub.getCity().trim().isEmpty() 
                    ? "N/A" 
                    : sub.getCity().trim()
                );
                copy.setStatus(sub.getStatus());
                return copy;
            })
            .collect(Collectors.toList());

        long total = displaySubs.size();
        long activeCount = rawSubs.stream()
            .filter(sub -> sub.getStatus() == Subscriber.Status.ACTIVE)
            .count();
        String checked = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                        .format(LocalDateTime.now());

        model.addAttribute("subscribers", displaySubs);  // ← CHANGED
        model.addAttribute("totalCount", total);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("lastChecked", checked);

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        return "admin";
        
    }

    @PostMapping("/delete/{id}")
    public String deleteSubscriber(@PathVariable("id") Long id) {  // ← EXPLICIT NAME
        subscriberRepository.deleteById(id);
        return "redirect:/admin";
    }

    @PostMapping("/trigger")
    public String triggerAlert(RedirectAttributes redirectAttributes) {
        List<Subscriber> allSubs = subscriberRepository.findAll();
        List<Subscriber> activeSubs = allSubs.stream()
            .filter(sub -> sub.getStatus() == Subscriber.Status.ACTIVE)
            .collect(Collectors.toList());

        if (allSubs.isEmpty()) {
            log.info("Trigger requested but no subscribers — skipping alert check");
            redirectAttributes.addFlashAttribute("message", "No subscribers to check.");
            redirectAttributes.addFlashAttribute("messageType", "info");
        } else if (activeSubs.isEmpty()) {
            log.info("Trigger requested but no active subscribers — skipping alert check");
            redirectAttributes.addFlashAttribute("message", "No active subscribers to check.");
            redirectAttributes.addFlashAttribute("messageType", "info");
        } else {
            log.info("Manual trigger: checking weather for {} active subscribers", activeSubs.size());
            alertScheduler.checkWeatherAndAlert(); // Uses active filter in WeatherService
            redirectAttributes.addFlashAttribute("message", 
                "Alert check triggered for " + activeSubs.size() + " active subscriber(s).");
            redirectAttributes.addFlashAttribute("messageType", "success");
        }
        return "redirect:/admin";
    }

    @PostMapping("/toggle/{id}")
    public String toggleSubscriberStatus(@PathVariable("id") Long id) {
        // Fetch from DB (original)
        Subscriber original = subscriberRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Subscriber not found"));
        
        original.toggleStatus();  // Toggle original
        subscriberRepository.save(original);  // Save to DB
        
        return "redirect:/admin";
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "Invalid";
        }
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];
    
        if (local.length() <= 1) {
            return email; // too short to mask
        }
    
        String maskedLocal = local.charAt(0) + 
                             "*".repeat(local.length() - 1);
    
        return maskedLocal + "@" + domain;
    }
    
}