package com.matchFit.post.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant


@Service
class PostActiveViewService(
    private val redisTemplate: StringRedisTemplate
) {
    fun heartbeat(postId: Long, viewerKey: String, now: Instant = Instant.now()): Long {
        val activePostKey = activePostKey(postId)
        val nowMs = now.toEpochMilli()
        val timeoutMs = ACTIVE_TIMEOUT.toMillis()
        val ttlSeconds = ACTIVE_KEY_TTL.seconds

        val result = redisTemplate.execute(
            HEARTBEAT_SCRIPT,
            listOf(activePostKey, ACTIVE_POSTS_KEY),
            nowMs.toString(),
            timeoutMs.toString(),
            viewerKey,
            postId.toString(),
            ttlSeconds.toString()
        )
        redisTemplate.expire(activePostKey, ACTIVE_KEY_TTL)
        return result ?: 0L
    }

    fun getActiveCount(postId: Long): Long {
        val count = redisTemplate.opsForZSet().size(activePostKey(postId))
        return count ?: 0L
    }

    fun getPopularPostIds(start: Long, end: Long): List<Long> {
        val ids = redisTemplate.opsForZSet().reverseRange(ACTIVE_POSTS_KEY, start, end)
        if (ids.isNullOrEmpty()) return emptyList()
        return ids.mapNotNull { it.toLongOrNull() }
    }

    fun getPopularPostCount(): Long {
        return redisTemplate.opsForZSet().size(ACTIVE_POSTS_KEY) ?: 0L
    }

    private fun activePostKey(postId: Long): String {
        return ACTIVE_POST_KEY_FMT.format(postId)
    }

    fun removeActivePost(postId: Long) {
        redisTemplate.opsForZSet().remove(ACTIVE_POSTS_KEY, postId.toString())
    }

    companion object {
        private val ACTIVE_TIMEOUT: Duration = Duration.ofSeconds(45)
        private val ACTIVE_KEY_TTL: Duration = ACTIVE_TIMEOUT.multipliedBy(2)
        private const val ACTIVE_POST_KEY_FMT = "active:post:%d"
        private const val ACTIVE_POSTS_KEY = "active:posts"

        private const val HEARTBEAT_LUA = """
            local activePostKey = KEYS[1]
            local activePostsKey = KEYS[2]
            local nowMs = tonumber(ARGV[1])
            local timeoutMs = tonumber(ARGV[2])
            local viewerKey = ARGV[3]
            local postId = ARGV[4]
            local ttlSeconds = tonumber(ARGV[5])

            redis.call('ZADD', activePostKey, nowMs, viewerKey)
            redis.call('ZREMRANGEBYSCORE', activePostKey, 0, nowMs - timeoutMs)
            redis.call('EXPIRE', activePostKey, ttlSeconds)
            local cnt = redis.call('ZCARD', activePostKey)

            if cnt == 0 then
                redis.call('DEL', activePostKey)
                redis.call('ZREM', activePostsKey, postId)
            else
                redis.call('ZADD', activePostsKey, cnt, postId)
            end

            return cnt
        """

        private val HEARTBEAT_SCRIPT: DefaultRedisScript<Long> = DefaultRedisScript<Long>().apply {
            setScriptText(HEARTBEAT_LUA)
            resultType = Long::class.java
        }
    }
}
