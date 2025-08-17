package com.matchFit.post.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchFit.post.dto.WeatherResponseDto;
import com.matchFit.post.entity.Town;
import com.matchFit.post.util.RegionCodeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 날짜와 지역 받아서 날씨 DTO 리턴
    public WeatherResponseDto getWeatherByDateAndTown(LocalDate date, Town town) {
        String regionCode = RegionCodeMapper.getRegionCode(town.name());
        if (regionCode == null) {
            return new WeatherResponseDto("지역 정보 없음", "-", "-", "-");
        }

        // 실제 API 요청 시간 기준으로 baseTime 세팅 (필요시 개선 가능)
        String baseTime = getBaseForecastTime();

        String url = UriComponentsBuilder.fromHttpUrl("https://apis.data.go.kr/1360000/MidFcstInfoService/getMidLandFcst")
            .queryParam("serviceKey", apiKey)
            .queryParam("numOfRows", 10)
            .queryParam("pageNo", 1)
            .queryParam("dataType", "JSON")
            .queryParam("regId", regionCode)
            .queryParam("tmFc", baseTime)
            .toUriString();

        try {
            String responseJson = restTemplate.getForObject(url, String.class);

            // JSON 파싱
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            if (items.isMissingNode() || !items.isArray()) {
                return new WeatherResponseDto("데이터 없음", "-", "-", "-");
            }

            // 예보 데이터 중 요청한 날짜에 맞는 정보 추출 (여기선 임시로 첫번째 데이터 사용)
            // 필요하면 date 기반 필터링 추가
            JsonNode forecast = items.get(0);

            // 예시) 중기예보 API에서 날씨 정보가 들어있는 키값에 맞게 수정 필요
            String weatherDesc = forecast.path("wf").asText("정보 없음");  // 예: "맑음"
            String temp = forecast.path("taMin").asText("-") + " ~ " + forecast.path("taMax").asText("-") + "°C";
            String precipitation = forecast.path("rnSt").asText("-") + "%";
            String wind = forecast.path("ws").asText("-") + "m/s";

            return new WeatherResponseDto(weatherDesc, temp, precipitation, wind);

        } catch (Exception e) {
            e.printStackTrace();
            return new WeatherResponseDto("파싱 오류", "-", "-", "-");
        }
    }

    private String getBaseForecastTime() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        String time = (hour < 18) ? "0600" : "1800";
        return now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + time;
    }
}
