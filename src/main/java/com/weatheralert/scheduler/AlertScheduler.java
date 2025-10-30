package com.weatheralert.scheduler;

import com.weatheralert.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertScheduler {
    private final WeatherService weatherService;

    /**
     * Runs every 30 minutes
     * F2P Test: Fails in BASE (missing WeatherService), Passes AFTER (service added)
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void checkWeatherAndAlert() {
        log.info("🌤️ Running weather alert check...");
        
        try {
            var alerts = weatherService.generateAlerts();
            log.info("✅ Generated {} alerts", alerts.size());
            
            // TODO: Send emails (Step 4)
            alerts.forEach(alert -> log.info("📧 {}", alert));
            
        } catch (Exception e) {
            log.error("❌ Weather alert check failed", e);
        }
    }
    
}
