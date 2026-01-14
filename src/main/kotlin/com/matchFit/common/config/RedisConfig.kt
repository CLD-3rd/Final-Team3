package com.matchFit.common.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {
    @Value("\${spring.data.redis.host}")
    private lateinit var host: String

    @Value("\${spring.data.redis.port}")
    private var port: Int = 0

    @Value("\${spring.data.redis.password}")
    private lateinit var password: String

    @Value("\${spring.data.redis.ssl.enabled:false}")
    private var sslEnabled: Boolean = false

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val standaloneConfig = RedisStandaloneConfiguration(host, port)
        standaloneConfig.setPassword(RedisPassword.of(password))

        val clientConfigBuilder = LettuceClientConfiguration.builder()
        if (sslEnabled) {
            clientConfigBuilder.useSsl()
        }

        return LettuceConnectionFactory(standaloneConfig, clientConfigBuilder.build())
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = redisConnectionFactory()
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = GenericJackson2JsonRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = GenericJackson2JsonRedisSerializer()
        return template
    }

    @Bean
    fun stringRedisTemplate(): StringRedisTemplate {
        val template = StringRedisTemplate()
        template.connectionFactory = redisConnectionFactory()
        return template
    }
}
