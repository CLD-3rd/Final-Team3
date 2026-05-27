package com.matchFit.user.token;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisPasswordResetToken {

    private static final String PREFIX = "RESET:";

    private final StringRedisTemplate redis;
    private final long ttlMinutes;

    public RedisPasswordResetToken(
            StringRedisTemplate redis,
            @Value("${app.password-reset.ttl-minutes:15}") long ttlMinutes
    ) {
        this.redis = redis;
        this.ttlMinutes = ttlMinutes;
    }

    public String issueToken(Long userId) {
        String token = generateToken();
        redis.opsForValue().set(key(token), userId.toString(), Duration.ofMinutes(ttlMinutes));
        return token;
    }

    public Long peekUserId(String token) {
        String value = redis.opsForValue().get(key(token));
        if (value == null) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Long consumeToken(String token) {
        String k = key(token);
        String userId = redis.opsForValue().get(k);
        if (userId == null) return null;
        redis.delete(k);
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String key(String token) {
        return PREFIX + token;
    }

    private String generateToken() {
        byte[] buf = new byte[32];
        new SecureRandom().nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
