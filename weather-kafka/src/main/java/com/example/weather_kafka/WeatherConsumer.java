package com.example.weather_kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherConsumer {

    private final ObjectMapper objectMapper;

    private final Map<String, Map<LocalDate, WeatherEvent>> cityWeather = new ConcurrentHashMap<>();

    private final Set<LocalDate> mushroomDaysTyumen = new CopyOnWriteArraySet<>();

    @KafkaListener(topics = "weather", groupId = "weather-group")
    public void listen(String message) {
        try {
            WeatherEvent event = objectMapper.readValue(message, WeatherEvent.class);
            log.info("Получено: {}", event);

            cityWeather.computeIfAbsent(event.getCity(), k -> new ConcurrentHashMap<>())
                    .put(event.getEventDate(), event);

            // Считать после получения 7 дней
            if (cityWeather.get(event.getCity()).size() >= 7) {
                printAnalytics(event.getCity());
            }

            // Спец-логика по грибам в Тюмени: >=2 дождливых дня подряд
            if ("Тюмень".equals(event.getCity())) {
                checkMushroomDayTyumen();
            }

        } catch (IOException e) {
            log.error("Ошибка при десериализации: {}", e.getMessage(), e);
        }
    }

    private void printAnalytics(String city) {
        Map<LocalDate, WeatherEvent> week = cityWeather.get(city);
        if (week == null || week.isEmpty()) {
            log.warn("Нет данных для анализа по городу {}", city);
            return;
        }

        long sunnyDays = week.values().stream()
                .filter(e -> "солнечно".equals(e.getCondition()))
                .count();
        long rainyDays = week.values().stream()
                .filter(e -> "дождь".equals(e.getCondition()))
                .count();
        Optional<WeatherEvent> hottest = week.values().stream()
                .max(Comparator.comparingInt(WeatherEvent::getTemperature));
        Optional<WeatherEvent> coldest = week.values().stream()
                .min(Comparator.comparingInt(WeatherEvent::getTemperature));
        double avgTemp = week.values().stream()
                .mapToInt(WeatherEvent::getTemperature)
                .average().orElse(0);

        log.info("Аналитика за неделю для города {}:", city);
        log.info("  Солнечных дней: {}", sunnyDays);
        log.info("  Дождливых дней: {}", rainyDays);
        log.info("  Самый жаркий день: {} ({}°C)",
                hottest.map(WeatherEvent::getEventDate).orElse(null),
                hottest.map(WeatherEvent::getTemperature).orElse(0));
        log.info("  Самый холодный день: {} ({}°C)",
                coldest.map(WeatherEvent::getEventDate).orElse(null),
                coldest.map(WeatherEvent::getTemperature).orElse(0));
        log.info("  Средняя температура: {:.1f}°C", avgTemp);

        if ("Магадан".equals(city) || "Чукотка".equals(city)) {
            log.info("  {}: солнечных дней — {}", city, sunnyDays);
        }
        if ("Питер".equals(city)) {
            log.info("  Питер: дождливых дней — {}", rainyDays);
        }
        if ("Тюмень".equals(city)) {
            log.info("  Тюмень: дни для грибов — {}", mushroomDaysTyumen);
        }

        week.clear();
    }

    private void checkMushroomDayTyumen() {
        Map<LocalDate, WeatherEvent> week = cityWeather.get("Тюмень");
        if (week == null || week.size() < 2) return;

        List<LocalDate> sortedDates = new ArrayList<>(week.keySet());
        sortedDates.sort(Comparator.naturalOrder());

        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate prevDate = sortedDates.get(i - 1);
            LocalDate currDate = sortedDates.get(i);

            WeatherEvent prev = week.get(prevDate);
            WeatherEvent curr = week.get(currDate);

            if (prev != null && curr != null &&
                    "дождь".equals(prev.getCondition()) &&
                    "дождь".equals(curr.getCondition())) {
                mushroomDaysTyumen.add(prevDate);
                mushroomDaysTyumen.add(currDate);
            }
        }
    }
}