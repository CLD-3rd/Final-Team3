package com.matchFit.weather.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponseDto {
	private LocalDate date;
    private LocalTime time;
    private String weather;
    private String precipitation;
    private String tempMin;
    private String tempMax;
    private String humidity;
    
}
