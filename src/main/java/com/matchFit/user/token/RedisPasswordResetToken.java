package com.matchFit.user.token;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class RedisPasswordResetToken {
	private final StringRedisTemplate redis;
	
	@Value("${app.password-reset.ttl-minutes:15}")
    private long ttlMinutes;

    private static final String PREFIX = "RESET:";

    public String issueToken(long userId) {
        String token = generateToken();
        redis.opsForValue().set(PREFIX + token, String.valueOf(userId), Duration.ofMinutes(ttlMinutes));
        return token;
    }

    public Long consumeToken(String token) {
        String key = PREFIX + token;
        String userId = redis.opsForValue().get(key);
        if (userId == null) return null;
        redis.delete(key);
        try {
        	return Long.parseLong(userId); 
        } catch (NumberFormatException e) { 
        	return null; 
        }
    }

    private String generateToken() {
        byte[] buf = new byte[32];
        new SecureRandom().nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
	
	
}
