package com.example.weather_kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_NAME = "weather";
    private static final List<String> CITIES = List.of(
            "Москва", "Магадан", "Чукотка", "Питер", "Тюмень"
    );
    private static final List<String> CONDITIONS = List.of(
            "солнечно", "облачно", "дождь"
    );
    private static final Random RANDOM = new Random();

    private LocalDate startDate = LocalDate.now().minusDays(6);
    private int dayOffset = 0;

    @Scheduled(fixedRate = 2000)
    public void sendWeatherEvent() {
        String city = CITIES.get(RANDOM.nextInt(CITIES.size()));
        String condition = CONDITIONS.get(RANDOM.nextInt(CONDITIONS.size()));
        int temperature = RANDOM.nextInt(36); // 0..35

        LocalDate eventDate = startDate.plusDays(dayOffset);
        dayOffset = (dayOffset + 1) % 7; // цикл по 7 дням

        WeatherEvent event = new WeatherEvent(city, eventDate, temperature, condition);
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC_NAME, city, json);
            log.info("Отправлено: {}", json);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации: {}", e.getMessage(), e);
        }
    }
}