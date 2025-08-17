package com.matchFit.notification.email.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.matchFit.notification.email.service.PostAlertService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailAlertScheduler {

    private final PostAlertService postAlertService;

    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시
    public void run() {
        postAlertService.notifyUsersAboutTomorrowMatches();
    }
}