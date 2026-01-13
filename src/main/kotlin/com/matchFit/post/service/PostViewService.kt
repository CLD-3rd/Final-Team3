package com.matchFit.post.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant


@Service
class PostViewService(
    private val redis: RedisTemplate<String, String>
) {
    fun recordView(postId: Long, userId: Long?) {
        if (userId == null) {
            return
        }
        val viewKey = String.format(VIEW_KEY_FMT, postId, userId)

        val isNew = redis.opsForValue().setIfAbsent(viewKey, "1", VIEW_TTL)
        if (isNew == true) {
            redis.opsForZSet().incrementScore(ZSET_KEY, postId.toString(), 1.0)

            val expireAt = Instant.now().epochSecond + VIEW_TTL.seconds
            val zsetMember = String.format("view:post_%d:user_%d", postId, userId)
            redis.opsForZSet().add("views:expiring", zsetMember, expireAt.toDouble())
        }
    }

    fun decrementViewCount(postId: Long) {
        redis.opsForZSet().incrementScore(ZSET_KEY, postId.toString(), -1.0)
    }

    fun getViewCounts(postIds: Collection<Long>): Map<Long, Long> {
        val result = mutableMapOf<Long, Long>()
        for (postId in postIds) {
            val score = redis.opsForZSet().score(ZSET_KEY, postId.toString())
            val count = score?.toLong() ?: 0L
            result[postId] = count
        }
        return result
    }

    companion object {
        private val VIEW_TTL: Duration = Duration.ofMinutes(1)
        private const val VIEW_KEY_FMT = "view:post_%d:user_%d"
        private const val ZSET_KEY = "views:posts:count"
    }
}
