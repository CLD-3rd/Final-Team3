package com.matchFit.common.config;

import com.matchFit.post.service.PostViewService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisListenerConfig {

    @Bean
    public RedisMessageListenerContainer redisContainer(
            LettuceConnectionFactory connectionFactory,
            ExpiredKeyListener expiredKeyListener
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(expiredKeyListener, new PatternTopic("__keyevent@0__:expired"));
        return container;
    }

    @Bean
    public ExpiredKeyListener expiredKeyListener(PostViewService postViewService) {
        return new ExpiredKeyListener(postViewService);
    }

    @RequiredArgsConstructor
    public static class ExpiredKeyListener implements MessageListener {

        private static final Pattern EXPIRED_VIEW_KEY = Pattern.compile("^view:post_(\\d+):user_(\\d+)$");

        private final PostViewService postViewService;

        @Override
        public void onMessage(Message message, byte[] pattern) {
            String expiredKey = message.toString();
            Matcher matcher = EXPIRED_VIEW_KEY.matcher(expiredKey);
            if (matcher.matches()) {
                long postId = Long.parseLong(matcher.group(1));
                postViewService.decrementViewCount(postId);
            }
        }
    }
}
