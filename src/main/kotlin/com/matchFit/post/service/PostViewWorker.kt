package com.matchFit.post.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


@Service
class PostViewWorker(
    private val redisTemplate: StringRedisTemplate,
    private val postViewService: PostViewService
) {
    init {
        startWorker()
    }

    private fun startWorker() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
            { processExpiredViews() },
            0,
            WORKER_INTERVAL,
            TimeUnit.MILLISECONDS
        )
    }

    private fun processExpiredViews() {
        val now = Instant.now().epochSecond
        val key = "views:expiring"

        val expiredKeys = redisTemplate.opsForZSet().rangeByScore(key, 0.0, now.toDouble())
        if (expiredKeys.isNullOrEmpty()) return

        val pattern = Pattern.compile("^view:post_(\\d+):user_(\\d+)$")
        for (expiredKey in expiredKeys) {
            val matcher = pattern.matcher(expiredKey)
            if (matcher.matches()) {
                val postId = matcher.group(1).toLong()
                postViewService.decrementViewCount(postId)
            }

            redisTemplate.opsForZSet().remove(key, expiredKey)
        }
    }

    companion object {
        private const val WORKER_INTERVAL = 5000L
    }
}
