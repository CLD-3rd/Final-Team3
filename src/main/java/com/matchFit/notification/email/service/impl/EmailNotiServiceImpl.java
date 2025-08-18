package com.matchFit.notification.email.service.impl;

import java.time.format.DateTimeFormatter;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.matchFit.notification.email.service.NotificationService;
import com.matchFit.post.entity.Post;
import com.matchFit.user.entity.User;
import com.matchFit.weather.dto.WeatherResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailNotiServiceImpl implements NotificationService {

	private final JavaMailSender mailSender;
	
	@Override
	public void sendMatchReminder(User user, Post post, WeatherResponseDto weather, String postUrl) {
	    String weatherDesc = weather != null ? weather.getWeather() : "정보 없음";
	    String precipitation = weather != null ? weather.getPrecipitation() : "-";
	    String tempMin = weather != null ? weather.getTempMin() : "-";
	    String tempMax = weather != null ? weather.getTempMax() : "-";
	    String humidity = weather != null ? weather.getHumidity() : "-";

	    String subject = "[MatchFit] 내일 경기 알림: " + post.getTitle();
	    String message = String.format(
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
	        user.getNickname(),
	        post.getTitle(),
	        post.getDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
	        post.getLocation(),
	        weatherDesc,
	        tempMin,
	        tempMax,
	        precipitation,
	        humidity,
	        postUrl
	    );

	    SimpleMailMessage mail = new SimpleMailMessage();
	    mail.setTo(user.getEmail());
	    mail.setSubject(subject);
	    mail.setText(message);

	    mailSender.send(mail);
	}


}
