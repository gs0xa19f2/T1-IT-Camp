package com.example.weather_kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherEvent {
    private String city;
    private LocalDate eventDate;
    private int temperature;
    private String condition; // солнечно, облачно, дождь
}