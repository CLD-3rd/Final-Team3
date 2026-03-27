package com.matchFit.post.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant


@Service
class PostActiveViewService(
    private val redisTemplate: StringRedisTemplate
) {
    fun recordActiveView(postId: Long, viewerKey: String) {
        val activePostKey = activePostKey(postId)
        val nowMs = Instant.now().toEpochMilli()
        val expiryMs = nowMs + ACTIVE_TIMEOUT.toMillis()

        redisTemplate.opsForZSet().add(activePostKey, viewerKey, expiryMs.toDouble())
        redisTemplate.opsForZSet().removeRangeByScore(activePostKey, 0.0, nowMs.toDouble())
        redisTemplate.expire(activePostKey, ACTIVE_KEY_TTL)

        val count = redisTemplate.opsForZSet().size(activePostKey) ?: 0L

        if (count == 0L) {
            redisTemplate.opsForZSet().remove(ACTIVE_POSTS_KEY, postId.toString())
        } else {
            redisTemplate.opsForZSet().add(ACTIVE_POSTS_KEY, postId.toString(), count.toDouble())
        }
    }

    fun getPopularPostIds(start: Long, end: Long): List<Long> {
        val ids = redisTemplate.opsForZSet().reverseRange(ACTIVE_POSTS_KEY, start, end)
        if (ids.isNullOrEmpty()) return emptyList()
        return ids.mapNotNull { it.toLongOrNull() }
    }

    fun getPopularPostCount(): Long {
        return redisTemplate.opsForZSet().size(ACTIVE_POSTS_KEY) ?: 0L
    }

    fun removeActivePost(postId: Long) {
        redisTemplate.opsForZSet().remove(ACTIVE_POSTS_KEY, postId.toString())
    }

    private fun activePostKey(postId: Long): String {
        return ACTIVE_POST_KEY_FMT.format(postId)
    }

    companion object {
        private val ACTIVE_TIMEOUT: Duration = Duration.ofSeconds(60)
        private val ACTIVE_KEY_TTL: Duration = ACTIVE_TIMEOUT.multipliedBy(2)
        private const val ACTIVE_POST_KEY_FMT = "active:post:%d"
        private const val ACTIVE_POSTS_KEY = "active:posts"
    }
}
