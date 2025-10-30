package com.weatheralert.controller;

import com.weatheralert.entity.Subscriber;
import com.weatheralert.repository.SubscriberRepository;
import com.weatheralert.scheduler.AlertScheduler;

import lombok.RequiredArgsConstructor;
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
    public String adminPage(Model model) {
        model.addAttribute("subscribers", subscriberRepository.findAll());
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