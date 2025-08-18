package com.matchFit.notification.email.service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.Status;
import com.matchFit.post.policy.PostNotificationPolicy;
import com.matchFit.post.repository.PostRepository;
import com.matchFit.user.entity.User;
import com.matchFit.weather.dto.WeatherResponseDto;
import com.matchFit.weather.service.ShortWeatherService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostAlertService {

    private final PostRepository postRepository;
    private final ShortWeatherService weatherService;
    private final NotificationService notificationService;

    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    public void notifyUsersAboutTomorrowMatches() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        // DB에서 조건에 맞는 포스트만 조회 (예: status = CLOSED, date = tomorrow)
        List<Post> posts = postRepository.findByStatusAndDate(Status.CLOSED, tomorrow);

        for (Post post : posts) {
            if (PostNotificationPolicy.isMatchTomorrow(post)) {
                LocalDateTime targetDateTime = tomorrow.atStartOfDay();
                WeatherResponseDto weather = null;

                try {
                    weather = weatherService.getShortTermWeatherByTown(post.getTown(), targetDateTime);
                } catch (Exception e) {
                    log.error("Weather API 호출 실패", e);
                    weather = null; // 실패 시 null 처리
                }

                User user = post.getUser();
                String postUrl = frontendUrl + "/post/" + post.getId();
                notificationService.sendMatchReminder(user, post, weather, postUrl);
            }
        }
    }
}
