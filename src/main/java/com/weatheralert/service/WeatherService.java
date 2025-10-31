package com.weatheralert.service;

import com.weatheralert.client.OpenMeteoClient;
import com.weatheralert.client.OpenMeteoClient.WeatherData;
import com.weatheralert.entity.Subscriber;
import com.weatheralert.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {
    private final OpenMeteoClient weatherClient;
    private final SubscriberRepository subscriberRepository;

    private static final double HEAT_THRESHOLD = 30.0;
    private static final double RAIN_THRESHOLD = 5.0;

    /**
     * Checks weather for all subscribers and returns alert messages.
     * In real app: would resolve city → lat/long via geocoding.
     */
    public List<String> generateAlerts() {
        List<String> alerts = new ArrayList<>();
        List<Subscriber> subscribers = subscriberRepository.findAll();
        
        // NEW: Filter active subscribers only
        List<Subscriber> activeSubs = subscribers.stream()
            .filter(sub -> sub.getStatus() == Subscriber.Status.ACTIVE)
            .collect(Collectors.toList());
    
        log.info("Generating weather alerts for {} subscribers", activeSubs.size());

        for (Subscriber sub : activeSubs) {
            try {
                // Hardcoded Berlin for now (52.52, 13.41)
                WeatherData data = weatherClient.fetchWeather(25.20, 55.27);

                StringBuilder msg = new StringBuilder("Weather Alert for ")
                        .append(sub.getEmail())
                        .append(" in ")
                        .append(sub.getCity())
                        .append(": ");

                boolean triggered = false;
                if (data.temperature() > HEAT_THRESHOLD) {
                    msg.append("HEAT (").append(String.format("%.1f", data.temperature())).append("°C) ");
                    triggered = true;
                }
                if (data.precipitation() > RAIN_THRESHOLD) {
                    msg.append("RAIN (").append(data.precipitation()).append("mm) ");
                    triggered = true;
                }

                if (triggered) {
                    alerts.add(msg.toString().trim());
                    log.info("Alert: {}", msg);
                }
            } catch (Exception e) {
                log.warn("Failed to check weather for {}: {}", sub.getEmail(), e.getMessage());
            }
        }

        return alerts;
    }

    public record AlertSummary(int totalSubscribers, int alertsTriggered, List<String> messages) {}
}

