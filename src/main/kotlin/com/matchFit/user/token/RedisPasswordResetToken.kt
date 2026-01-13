package com.matchFit.user.token

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.time.Duration
import java.util.Base64


@Component
class RedisPasswordResetToken(
    private val redis: StringRedisTemplate,
    @Value("\${app.password-reset.ttl-minutes:15}") private val ttlMinutes: Long
) {
    fun issueToken(userId: Long): String {
        val token = generateToken()
        redis.opsForValue().set(key(token), userId.toString(), Duration.ofMinutes(ttlMinutes))
        return token
    }

    fun peekUserId(token: String): Long? {
        val value = redis.opsForValue().get(key(token)) ?: return null
        return value.toLongOrNull()
    }

    fun consumeToken(token: String): Long? {
        val k = key(token)
        val userId = redis.opsForValue().get(k) ?: return null
        redis.delete(k)
        return userId.toLongOrNull()
    }

    private fun key(token: String): String = PREFIX + token

    private fun generateToken(): String {
        val buf = ByteArray(32)
        SecureRandom().nextBytes(buf)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf)
    }

    companion object {
        private const val PREFIX = "RESET:"
    }
}
