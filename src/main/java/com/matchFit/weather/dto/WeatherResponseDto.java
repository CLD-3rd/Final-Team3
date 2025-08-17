package com.matchFit.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponseDto {
    private String weatherDescription;  // 예: "맑음", "흐림", "비"
    private String temperature;          // 예: "25°C"
    private String precipitationProbability;  // 예: "30%"
    private String windSpeed;            // 예: "5m/s"
}