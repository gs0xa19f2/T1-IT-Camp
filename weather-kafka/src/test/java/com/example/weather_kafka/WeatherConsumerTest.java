package com.example.weather_kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WeatherConsumerTest {

    @Autowired
    private WeatherConsumer consumer;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testConsumerInitialized() {
        assertNotNull(consumer);
    }

    @Test
    void testListenMethod() throws Exception {
        WeatherEvent event = new WeatherEvent(
                "Тестовый город",
                LocalDate.now(),
                20,
                "облачно"
        );

        String json = objectMapper.writeValueAsString(event);

        assertDoesNotThrow(() -> consumer.listen(json));
    }
}