package com.matchFit.notification.email.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.Status;
import com.matchFit.post.policy.PostNotificationPolicy;
import com.matchFit.post.repository.PostRepository;
import com.matchFit.user.entity.User;
import com.matchFit.weather.dto.WeatherResponseDto;
import com.matchFit.weather.service.WeatherService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostAlertService {

    private final PostRepository postRepository;
    private final WeatherService weatherService;
    private final NotificationService notificationService;

    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    public void notifyUsersAboutTomorrowMatches() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        // DB에서 조건에 맞는 포스트만 조회 (예: status = CLOSED, date = tomorrow)
        List<Post> posts = postRepository.findByStatusAndDate(Status.CLOSED, tomorrow);

        for (Post post : posts) {
            // 조건 정책 검사 (필요하다면)
            if (PostNotificationPolicy.isMatchTomorrow(post)) {
                WeatherResponseDto weather = weatherService.getWeatherByDateAndTown(tomorrow, post.getTown());
                User user = post.getUser();
                String postUrl = frontendUrl + "/post/" + post.getId();
                notificationService.sendMatchReminder(user, post, weather, postUrl);
            }
        }
    }
}
