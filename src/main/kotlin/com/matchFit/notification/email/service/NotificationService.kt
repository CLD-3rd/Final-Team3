package com.matchFit.notification.email.service

import com.matchFit.post.entity.Post
import com.matchFit.user.entity.User
import com.matchFit.weather.dto.WeatherResponseDto


interface NotificationService {
    fun sendMatchReminder(user: User, post: Post, weather: WeatherResponseDto?, postUrl: String)
}
