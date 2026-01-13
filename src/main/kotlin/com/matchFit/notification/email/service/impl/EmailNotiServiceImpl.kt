package com.matchFit.notification.email.service.impl

import com.matchFit.notification.email.service.NotificationService
import com.matchFit.post.entity.Post
import com.matchFit.user.entity.User
import com.matchFit.weather.dto.WeatherResponseDto
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter


@Service
class EmailNotiServiceImpl(
    private val mailSender: JavaMailSender
) : NotificationService {
    override fun sendMatchReminder(user: User, post: Post, weather: WeatherResponseDto?, postUrl: String) {
        try {
            val weatherDesc = weather?.weather ?: "정보 없음"
            val precipitation = weather?.precipitation ?: "-"
            val tempMin = weather?.tempMin ?: "-"
            val tempMax = weather?.tempMax ?: "-"
            val humidity = weather?.humidity ?: "-"

            val subject = "[MatchFit] 내일 경기 알림: ${post.title}"
            val message = String.format(
                """
                안녕하세요 %s님,

                내일 예정된 '%s' 경기가 있습니다.

                🕒 시간: %s
                📍 장소: %s
                🌤️ 날씨: %s
                🌡️ 최저/최고 온도: %s°C / %s°C
                💧 강수 확률: %s
                💦 습도: %s

                👉 상세 보기: %s

                좋은 하루 되세요!
                """,
                user.nickname,
                post.title,
                post.date.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                post.location,
                weatherDesc,
                tempMin,
                tempMax,
                precipitation,
                humidity,
                postUrl
            )

            val mail = SimpleMailMessage().apply {
                setTo(user.email)
                setSubject(subject)
                setText(message)
            }

            mailSender.send(mail)
        } catch (ex: Exception) {
            System.err.println("메일 전송 실패: ${ex.message}")
        }
    }
}
