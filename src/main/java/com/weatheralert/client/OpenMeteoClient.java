package com.weatheralert.client;

import java.io.IOException;
import java.time.Duration;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenMeteoClient {
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Fetches current weather from Open-Meteo API.
     * Caches result for 5 minutes.
     */
    @Cacheable(value = "weather", key = "#latitude + '_' + #longitude")
    public WeatherData fetchWeather(double latitude, double longitude) {
        String url = String.format(
            "https://api.open-meteo.com/v1/forecast" +
            "?latitude=%.2f&longitude=%.2f" +
            "&current=temperature_2m,precipitation,weather_code" +
            "&timezone=auto",
            latitude, longitude
        );

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "WeatherAlertNotifier/1.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("Open-Meteo API error: {} {}", response.code(), response.message());
                throw new WeatherApiException("API returned " + response.code());
            }

            String body = response.body().string();
            JsonNode root = mapper.readTree(body);
            JsonNode current = root.path("current");

            double temp = current.path("temperature_2m").asDouble(Double.NaN);
            double precip = current.path("precipitation").asDouble(0.0);
            int code = current.path("weather_code").asInt(-1);

            if (Double.isNaN(temp)) {
                throw new WeatherApiException("Missing temperature in response");
            }

            return new WeatherData(temp, precip, code, root.path("timezone").asText());
        } catch (IOException e) {
            log.error("Failed to fetch weather for ({}, {})", latitude, longitude, e);
            throw new WeatherApiException("Network error", e);
        }
    }

    public record WeatherData(
            double temperature,
            double precipitation,
            int weatherCode,
            String timezone
    ) {}

    public static class WeatherApiException extends RuntimeException {
        public WeatherApiException(String message) { super(message); }
        public WeatherApiException(String message, Throwable cause) { super(message, cause); }
    }
}
