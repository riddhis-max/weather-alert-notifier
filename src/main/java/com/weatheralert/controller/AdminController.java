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
                copy.setEmail(sub.getEmail());
                copy.setCity(
                    sub.getCity() == null || sub.getCity().trim().isEmpty() 
                    ? "N/A" 
                    : sub.getCity().trim()
                );
                return copy;
            })
            .collect(Collectors.toList());

        long total = displaySubs.size();
        long active = displaySubs.stream()
                                .filter(s -> !s.getCity().equals("N/A"))
                                .count();
        String checked = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                        .format(LocalDateTime.now());

        model.addAttribute("subscribers", displaySubs);  // ← CHANGED
        model.addAttribute("totalCount", total);
        model.addAttribute("activeCount", active);
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
        List<Subscriber> subs = subscriberRepository.findAll();

        if (subs.isEmpty()) {
            log.info("Trigger requested but no subscribers — skipping alert check");
            redirectAttributes.addFlashAttribute("message", "No subscribers to check.");
            redirectAttributes.addFlashAttribute("messageType", "info");
        } else {
            log.info("Manual trigger: checking weather for {} subscribers", subs.size());
            alertScheduler.checkWeatherAndAlert();
            redirectAttributes.addFlashAttribute("message", "Alert check triggered for " + subs.size() + " subscribers.");
            redirectAttributes.addFlashAttribute("messageType", "success");
        }

        return "redirect:/admin";
}
}