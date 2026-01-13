package com.matchFit.notification.email.scheduler

import com.matchFit.notification.email.service.PostAlertService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class EmailAlertScheduler(
    private val postAlertService: PostAlertService
) {
    @Scheduled(cron = "0 0 9 * * *")
    fun run() {
        postAlertService.notifyUsersAboutTomorrowMatches()
    }
}
