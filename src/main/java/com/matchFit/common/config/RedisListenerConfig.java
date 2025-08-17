//package com.matchFit.common.config;
//
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.Message;
//import org.springframework.data.redis.connection.MessageListener;
//import org.springframework.data.redis.connection.RedisConnection;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.listener.PatternTopic;
//import org.springframework.data.redis.listener.RedisMessageListenerContainer;
//
//import com.matchFit.post.service.PostViewService;
//
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//
//
//
//@Configuration
//@RequiredArgsConstructor
//public class RedisListenerConfig {
//	
//	private final RedisConnectionFactory connectionFactory;
//	
//    @Bean
//    public RedisMessageListenerContainer redisContainer(
//            LettuceConnectionFactory connectionFactory,
//            ExpiredKeyListener expiredKeyListener) {
//
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//        // "__keyevent@0__:expired" 은 DB 0번에서 만료 이벤트를 구독
//        container.addMessageListener(
//            expiredKeyListener,
//            new PatternTopic("__keyevent@0__:expired")
//        );
//        return container;
//    }
//
//    @Bean
//    public ExpiredKeyListener expiredKeyListener(PostViewService postViewService) {
//        return new ExpiredKeyListener(postViewService);
//    }
//    
//    // @PostConstruct
//    // public void enableKeyspaceNotifications() {
//    //     RedisConnection conn = null;
//    //     try {
//    //         conn = connectionFactory.getConnection();
//    //         // 'Ex' : E = Keyevent, x = expired 이벤트
//    //         conn.setConfig("notify-keyspace-events", "Ex");
//    //     } finally {
//    //         if (conn != null) {
//    //             conn.close();
//    //         }
//    //     }
//    // }
//    
//    private class ExpiredKeyListener implements MessageListener {
//        private final PostViewService postViewService;
//        // view:{postId}:user:{userId} 패턴에서 postId, userId 추출
//        private static final Pattern EXPIRED_VIEW_KEY =
//            Pattern.compile("^view:post_(\\d+):user_(\\d+)$");
//
//        public ExpiredKeyListener(PostViewService postViewService) {
//            this.postViewService = postViewService;
//        }
//
//        @Override
//        public void onMessage(Message message, byte[] pattern) {
//            String expiredKey = message.toString();
//            Matcher m = EXPIRED_VIEW_KEY.matcher(expiredKey);
//            if (m.matches()) {
//                Long postId = Long.valueOf(m.group(1));
//                // Long userId = Long.valueOf(m.group(2)); // 필요 없으면 생략
//                // 만료됐으니 ZSet 점수 1 차감
//                postViewService.decrementViewCount(postId);
//            }
//        }
//    }
//
//}
