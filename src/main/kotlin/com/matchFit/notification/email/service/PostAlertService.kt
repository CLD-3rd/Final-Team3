package com.matchFit.notification.email.service

import com.matchFit.post.entity.Status
import com.matchFit.post.policy.PostNotificationPolicy
import com.matchFit.post.repository.PostRepository
import com.matchFit.weather.service.ShortWeatherService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDate


@Service
class PostAlertService(
    private val postRepository: PostRepository,
    private val weatherService: ShortWeatherService,
    private val notificationService: NotificationService,
    @Value("\${app.frontend.url}") private val frontendUrl: String
) {
    private val log = LoggerFactory.getLogger(PostAlertService::class.java)

    fun notifyUsersAboutTomorrowMatches() {
        val tomorrow = LocalDate.now().plusDays(1)
        val posts = postRepository.findByStatusAndDate(Status.CLOSED, tomorrow)

        for (post in posts) {
            if (PostNotificationPolicy.isMatchTomorrow(post)) {
                val targetDateTime = tomorrow.atStartOfDay()
                val weather = try {
                    weatherService.getShortTermWeatherByTown(post.town, targetDateTime)
                } catch (ex: Exception) {
                    log.error("Weather API 호출 실패", ex)
                    null
                }

                val postUrl = "${frontendUrl}/post/${post.id}"
                notificationService.sendMatchReminder(post.user, post, weather, postUrl)
            }
        }
    }
}
