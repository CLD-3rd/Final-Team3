package com.matchFit.post.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostViewService {

    private static final Duration VIEW_TTL = Duration.ofMinutes(1);
    private static final String VIEW_KEY_FMT = "view:post_%d:user_%d";
    private static final String ZSET_KEY = "views:posts:count";

    private final RedisTemplate<String, String> redis;

    public void recordView(Long postId, Long userId) {
        if (userId == null) {
            return;
        }
        String viewKey = String.format(VIEW_KEY_FMT, postId, userId);

        Boolean isNew = redis.opsForValue().setIfAbsent(viewKey, "1", VIEW_TTL);
        if (Boolean.TRUE.equals(isNew)) {
            redis.opsForZSet().incrementScore(ZSET_KEY, postId.toString(), 1.0);
        }
    }

    public void decrementViewCount(long postId) {
        redis.opsForZSet().incrementScore(ZSET_KEY, Long.toString(postId), -1.0);
    }

    public Map<Long, Long> getViewCounts(Collection<Long> postIds) {
        Map<Long, Long> result = new HashMap<>();
        for (Long postId : postIds) {
            Double score = redis.opsForZSet().score(ZSET_KEY, postId.toString());
            long count = score == null ? 0L : score.longValue();
            result.put(postId, count);
        }
        return result;
    }

    public List<Long> getPopularPostIds(long start, long end) {
        Set<String> ids = redis.opsForZSet().reverseRange(ZSET_KEY, start, end);
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        List<Long> result = new ArrayList<>(ids.size());
        for (String id : ids) {
            try {
                result.add(Long.parseLong(id));
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    public long getPopularPostCount() {
        Long size = redis.opsForZSet().size(ZSET_KEY);
        return size == null ? 0L : size;
    }
}
