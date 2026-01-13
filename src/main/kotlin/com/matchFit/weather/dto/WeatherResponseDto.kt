package com.matchFit.weather.dto

import java.time.LocalDate
import java.time.LocalTime


data class WeatherResponseDto(
    val date: LocalDate?,
    val time: LocalTime?,
    val weather: String,
    val precipitation: String,
    val tempMin: String,
    val tempMax: String,
    val humidity: String
)
