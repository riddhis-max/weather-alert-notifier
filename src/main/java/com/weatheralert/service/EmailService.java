package com.weatheralert.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@ConditionalOnProperty(name = "app.email.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendAlert(String to, String subject, String body) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // HTML

            mailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", to, e);
            throw new RuntimeException("Email send failed", e);
        }
    }

    public void sendWeatherAlerts(java.util.List<String> alerts) {
        if (alerts.isEmpty()) return;

        StringBuilder html = new StringBuilder("""
            <h2>Weather Alerts</h2>
            <ul>
            """);
        alerts.forEach(a -> html.append("<li>").append(a).append("</li>"));
        html.append("</ul>");

        // Send to all (or admin)
        sendAlert("admin@example.com", "Weather Alerts Generated", html.toString());
    }
}
