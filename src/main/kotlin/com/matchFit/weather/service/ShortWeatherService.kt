package com.matchFit.weather.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.matchFit.post.entity.Town
import com.matchFit.weather.dto.WeatherResponseDto
import com.matchFit.weather.util.RegionCodeMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Service
class ShortWeatherService(
    @Value("\${weather.api.key}") private val apiKey: String
) {
    private val log = LoggerFactory.getLogger(ShortWeatherService::class.java)
    private val restTemplate = RestTemplate()
    private val objectMapper = ObjectMapper()

    fun getShortTermWeatherByTown(town: Town): WeatherResponseDto =
        getShortTermWeatherByTown(town, LocalDateTime.now())

    fun getShortTermWeatherByTown(town: Town, targetTime: LocalDateTime): WeatherResponseDto {
        val regionCode = RegionCodeMapper.getRegionCode(town.name)
        if (regionCode == null) {
            return WeatherResponseDto(null, null, "지역 정보 없음", "-", "-", "-", "-")
        }

        val baseDate = calculateBaseDate(LocalDateTime.now())
        val baseTime = calculateBaseTime(LocalDateTime.now())

        val uri: URI = UriComponentsBuilder.fromHttpUrl("https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst")
            .queryParam("serviceKey", apiKey)
            .queryParam("numOfRows", 1000)
            .queryParam("pageNo", 1)
            .queryParam("dataType", "JSON")
            .queryParam("base_date", baseDate)
            .queryParam("base_time", baseTime)
            .queryParam("nx", RegionCodeMapper.getNx(town.name))
            .queryParam("ny", RegionCodeMapper.getNy(town.name))
            .build(true)
            .toUri()

        return try {
            val responseJson = restTemplate.getForObject(uri, String::class.java)
            val root = objectMapper.readTree(responseJson)
            val items = root.path("response").path("body").path("items").path("item")

            if (items.isMissingNode || !items.isArray) {
                WeatherResponseDto(null, null, "-", "-", "-", "-", "-")
            } else {
                parseShortTermForecast(items, targetTime)
            }
        } catch (ex: Exception) {
            log.warn("Weather API parsing failed", ex)
            WeatherResponseDto(null, null, "파싱 오류", "파싱 오류", "파싱 오류", "파싱 오류", "파싱 오류")
        }
    }

    private fun calculateBaseDate(time: LocalDateTime): String {
        return if (time.hour < 2 || (time.hour == 2 && time.minute < 10)) {
            time.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        } else {
            time.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        }
    }

    private fun calculateBaseTime(time: LocalDateTime): String {
        val availableHours = intArrayOf(2, 5, 8, 11, 14, 17, 20, 23)
        val hour = time.hour
        val minute = time.minute

        for (i in availableHours.indices.reversed()) {
            val h = availableHours[i]
            if (hour > h || (hour == h && minute >= 10)) {
                return String.format("%02d00", h)
            }
        }

        return String.format("%02d00", 23)
    }

    private fun parseShortTermForecast(items: JsonNode, targetTime: LocalDateTime): WeatherResponseDto {
        val categoryMap = mutableMapOf<String, String>()
        var closestForecastDateTime: LocalDateTime? = null
        var minDiff = Long.MAX_VALUE

        for (item in items) {
            val category = item.path("category").asText()
            val fcstDate = item.path("fcstDate").asText()
            val fcstTime = item.path("fcstTime").asText()
            val value = item.path("fcstValue").asText()

            val forecastTime = LocalDateTime.parse(
                fcstDate + fcstTime,
                DateTimeFormatter.ofPattern("yyyyMMddHHmm")
            )

            if (forecastTime.isBefore(targetTime)) {
                continue
            }

            val diff = Duration.between(targetTime, forecastTime).toMinutes()
            if (diff < minDiff) {
                minDiff = diff
                closestForecastDateTime = forecastTime
                categoryMap.clear()
                categoryMap[category] = value
            } else if (diff == minDiff) {
                categoryMap[category] = value
            }
        }

        if (closestForecastDateTime == null) {
            return WeatherResponseDto(null, null, "예보 없음", "-", "-", "-", "-")
        }

        return WeatherResponseDto(
            closestForecastDateTime.toLocalDate(),
            closestForecastDateTime.toLocalTime(),
            categoryMap["SKY"] ?: "-",
            categoryMap["POP"] ?: "-",
            categoryMap["TMN"] ?: "-",
            categoryMap["TMX"] ?: "-",
            categoryMap["REH"] ?: "-"
        )
    }
}
