package com.matchFit.post.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PostViewService {
	private static final Duration VIEW_TTL = Duration.ofMinutes(1);
	private static final String VIEW_KEY_FMT = "view:post_%d:user_%d";    // view:post:3:user:1
	private static final String ZSET_KEY     = "views:posts:count";      // views:posts:count


    private final RedisTemplate<String,String> redis;

    public PostViewService(RedisTemplate<String,String> redis) {
        this.redis = redis;
    }

    /**
     * 사용자가 postId를 조회했을 때 호출.
     * 10분 내에 중복 호출은 무시, 최초 호출일 때만 ZSET에 1 더함.
     */
    public void recordView(Long postId, Long userId) {
        String viewKey = String.format(VIEW_KEY_FMT, postId, userId);

        // 기존과 동일하게 최초 조회 체크
        Boolean isNew = redis.opsForValue()
            .setIfAbsent(viewKey, "1", VIEW_TTL); // VIEW_TTL = Duration.ofMinutes(1)
        if (Boolean.TRUE.equals(isNew)) {
            // 1️⃣ 조회수 증가 (실제 카운트)
            redis.opsForZSet().incrementScore(ZSET_KEY, postId.toString(), 1);

            // 2️⃣ ZSET에 만료 시각 저장 (worker가 TTL 체크)
            long expireAt = Instant.now().getEpochSecond() + VIEW_TTL.getSeconds();
            String zsetMember = String.format("view:post_%d:user_%d", postId, userId);
            redis.opsForZSet().add("views:expiring", zsetMember, expireAt);
        }
    }

    
    /** 만료될 때 호출해서 점수를 1 차감 */
    public void decrementViewCount(Long postId) {
        redis.opsForZSet().incrementScore(ZSET_KEY, postId.toString(), -1);
    }

    /**
     * postId 리스트에 대해 각각 누적 조회수를 가져옴.
     * 없으면 0 반환.
     */
    public Map<Long, Long> getViewCounts(Collection<Long> postIds) {
        Map<Long, Long> result = new HashMap<>();
        for (Long postId : postIds) {
            // Redis ZSet 에서는 score(key, member) 가 단일 조회만 지원합니다.
            Double score = redis.opsForZSet().score(ZSET_KEY, postId.toString());
            long count = (score == null) ? 0L : score.longValue();
            result.put(postId, count);
        }
        return result;
    }
}
