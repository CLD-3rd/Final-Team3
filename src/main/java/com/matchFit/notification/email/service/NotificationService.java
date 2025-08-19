package com.matchFit.notification.email.service;

import com.matchFit.post.entity.Post;
import com.matchFit.user.entity.User;
import com.matchFit.weather.dto.WeatherResponseDto;

public interface NotificationService {
    void sendMatchReminder(User user, Post post, WeatherResponseDto weather, String postUrl);
}

