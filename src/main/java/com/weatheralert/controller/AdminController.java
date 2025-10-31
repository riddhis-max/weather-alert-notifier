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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SubscriberRepository subscriberRepository;
    private final AlertScheduler alertScheduler;

    @GetMapping
    public String adminPage(Model model, HttpServletResponse response) {
        List<Subscriber> all = subscriberRepository.findAll();
        long total = all.size();
        long active = all.stream()
                        .filter(s -> s.getCity() != null && !s.getCity().trim().isEmpty())
                        .count();
        String checked = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                        .format(LocalDateTime.now());

        model.addAttribute("subscribers", all);
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

    @PostMapping("/trigger")  // ← ADD THIS
    public String triggerAlert() {
        alertScheduler.checkWeatherAndAlert();
        return "redirect:/admin";
    }
}