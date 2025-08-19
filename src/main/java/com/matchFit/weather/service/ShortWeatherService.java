package com.matchFit.weather.service;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchFit.post.entity.Town;
import com.matchFit.weather.dto.WeatherResponseDto;
import com.matchFit.weather.util.RegionCodeMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortWeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    //기본 현재 시간 기준 예보
    public WeatherResponseDto getShortTermWeatherByTown(Town town) {
        return getShortTermWeatherByTown(town, LocalDateTime.now());
    }

    // 모임 시간 등 targetTime 기준 예보
    public WeatherResponseDto getShortTermWeatherByTown(Town town, LocalDateTime targetTime) {
        String regionCode = RegionCodeMapper.getRegionCode(town.name());
        if (regionCode == null) {
            return new WeatherResponseDto(null, null, "지역 정보 없음", "-", "-", "-", "-");
        }

        // 기상청 단기예보는 base_time 기준으로 최신 데이터만 줌
        String baseDate = calculateBaseDate(LocalDateTime.now());
        String baseTime = calculateBaseTime(LocalDateTime.now());

        URI uri = UriComponentsBuilder.fromHttpUrl("https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst")
                .queryParam("serviceKey", apiKey)
                .queryParam("numOfRows", 1000)
                .queryParam("pageNo", 1)
                .queryParam("dataType", "JSON")
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", RegionCodeMapper.getNx(town.name()))
                .queryParam("ny", RegionCodeMapper.getNy(town.name()))
                .build(true)
                .toUri();

        try {
            String responseJson = restTemplate.getForObject(uri, String.class);

            System.out.println("API 호출 URL: " + uri);
            System.out.println("API 응답 JSON: " + responseJson);

            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            if (items.isMissingNode() || !items.isArray()) {
                return new WeatherResponseDto(null, null, "-", "-", "-", "-", "-");
            }

            return parseShortTermForecast(items, targetTime);

        } catch (Exception e) {
            e.printStackTrace();
            return new WeatherResponseDto(null, null, "파싱 오류", "파싱 오류", "파싱 오류", "파싱 오류", "파싱 오류");
        }
    }

    private String calculateBaseDate(LocalDateTime time) {
        // 2시간마다 발표되는 기준 시간 이전의 가장 가까운 시간 선택
        if (time.getHour() < 2 || (time.getHour() == 2 && time.getMinute() < 10)) {
            return time.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
        return time.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private String calculateBaseTime(LocalDateTime time) {
        int[] availableHours = {2, 5, 8, 11, 14, 17, 20, 23};
        int hour = time.getHour();
        int minute = time.getMinute();
        
        // 기본값: 가장 마지막(=전날 23시 발표)
        int selectedHour = 23;

        for (int i = availableHours.length - 1; i >= 0; i--) {
            int h = availableHours[i];
            // 발표 시간 기준으로 10분 지난 시점부터 사용 가능
            if (hour > h || (hour == h && minute >= 10)) {
                return String.format("%02d00", h);
//                break;
            }
        }

        return String.format("%02d00", selectedHour);
    }


    private WeatherResponseDto parseShortTermForecast(JsonNode items, LocalDateTime targetTime) {
        Map<String, String> categoryMap = new HashMap<>();
        LocalDateTime closestForecastDateTime = null;
        long minDiff = Long.MAX_VALUE;
        
        String tmx = "-";
        String tmn = "-";

        for (JsonNode item : items) {
            String category = item.path("category").asText(); // POP, SKY, TMP, etc.
            String fcstDate = item.path("fcstDate").asText();
            String fcstTime = item.path("fcstTime").asText();
            String value = item.path("fcstValue").asText();

            LocalDateTime forecastTime = LocalDateTime.parse(fcstDate + fcstTime, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            long diff = Math.abs(Duration.between(forecastTime, targetTime).toMinutes());

            if (diff < minDiff) {
                minDiff = diff;
                closestForecastDateTime = forecastTime;
                categoryMap.clear();
                categoryMap.put(category, value);
            } else if (diff == minDiff) {
                categoryMap.put(category, value);
            }
        }

        if (closestForecastDateTime == null) {
            return new WeatherResponseDto(null, null, "-", "-", "-", "-", "-");
        }

        return new WeatherResponseDto(
                closestForecastDateTime.toLocalDate(),
                closestForecastDateTime.toLocalTime(),
                categoryMap.getOrDefault("SKY", "-"),
                categoryMap.getOrDefault("POP", "-"),
                categoryMap.getOrDefault("TMN", "-"),
                categoryMap.getOrDefault("TMX", "-"),
                categoryMap.getOrDefault("REH", "-")
        );
    }
}
