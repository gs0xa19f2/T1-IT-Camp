package com.example.weather_kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WeatherEventTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerialization() throws Exception {
        WeatherEvent event = new WeatherEvent(
                "Москва",
                LocalDate.of(2025, 7, 28),
                25,
                "солнечно"
        );

        String json = objectMapper.writeValueAsString(event);
        WeatherEvent deserialized = objectMapper.readValue(json, WeatherEvent.class);

        assertEquals(event.getCity(), deserialized.getCity());
        assertEquals(event.getEventDate(), deserialized.getEventDate());
        assertEquals(event.getTemperature(), deserialized.getTemperature());
        assertEquals(event.getCondition(), deserialized.getCondition());
    }
}