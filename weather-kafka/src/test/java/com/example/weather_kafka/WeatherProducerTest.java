package com.example.weather_kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class WeatherProducerTest {

    @Test
    void testSendWeatherEvent() {
        KafkaTemplate<String, String> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        WeatherProducer producer = new WeatherProducer(kafkaTemplate, objectMapper);
        producer.sendWeatherEvent();

        verify(kafkaTemplate, times(1)).send(eq("weather"), anyString(), anyString());
    }
}