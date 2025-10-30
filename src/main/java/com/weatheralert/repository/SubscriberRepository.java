package com.weatheralert.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weatheralert.entity.Subscriber;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    
}
