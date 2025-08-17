package com.matchFit.post.service;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PostViewWorker {

    private final StringRedisTemplate redisTemplate;
    private final PostViewService postViewService;

    // worker 주기(ms)
    private static final long WORKER_INTERVAL = 5000L;

    public PostViewWorker(StringRedisTemplate redisTemplate, PostViewService postViewService) {
        this.redisTemplate = redisTemplate;
        this.postViewService = postViewService;
        startWorker();
    }

    private void startWorker() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
            this::processExpiredViews, 0, WORKER_INTERVAL, TimeUnit.MILLISECONDS
        );
    }

    private void processExpiredViews() {
        long now = Instant.now().getEpochSecond();
        String key = "views:expiring";

        // 만료된 키 조회
        Set<String> expiredKeys = redisTemplate.opsForZSet().rangeByScore(key, 0, now);

        if (expiredKeys == null || expiredKeys.isEmpty()) return;

        for (String expiredKey : expiredKeys) {
            // postId 추출
            Matcher m = Pattern.compile("^view:post_(\\d+):user_(\\d+)$").matcher(expiredKey);
            if (m.matches()) {
                Long postId = Long.valueOf(m.group(1));
                postViewService.decrementViewCount(postId);
            }

            // ZSET에서 제거
            redisTemplate.opsForZSet().remove(key, expiredKey);
        }
    }
}
