package com.matchFit.common.config

import com.matchFit.post.service.PostViewService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import java.util.regex.Pattern

@Configuration
class RedisListenerConfig(
    private val connectionFactory: RedisConnectionFactory
) {
    @Bean
    fun redisContainer(
        connectionFactory: LettuceConnectionFactory,
        expiredKeyListener: ExpiredKeyListener
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)
        container.addMessageListener(expiredKeyListener, PatternTopic("__keyevent@0__:expired"))
        return container
    }

    @Bean
    fun expiredKeyListener(
        postViewService: PostViewService
    ): ExpiredKeyListener =
        ExpiredKeyListener(postViewService)

    class ExpiredKeyListener(
        private val postViewService: PostViewService
    ) : MessageListener {
        companion object {
            private val EXPIRED_VIEW_KEY: Pattern =
                Pattern.compile("^view:post_(\\d+):user_(\\d+)$")
        }

        override fun onMessage(message: Message, pattern: ByteArray?) {
            val expiredKey = message.toString()
            val matcher = EXPIRED_VIEW_KEY.matcher(expiredKey)
            if (matcher.matches()) {
                val postId = matcher.group(1).toLong()
                postViewService.decrementViewCount(postId)
            }
        }
    }
}
