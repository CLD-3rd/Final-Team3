package com.matchFit.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
	
	@Value("${spring.data.redis.host}")
    private String host;
    
    @Value("${spring.data.redis.port}")
    private int port;
    
    @Value("${spring.data.redis.password}") 
    String password;

    @Value("${spring.redis.ssl.enabled:false}") 
    boolean sslEnabled;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
    	RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        config.setPassword(password);
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
                LettuceClientConfiguration.builder()
                    .commandTimeout(Duration.ofMillis(
                       Integer.parseInt(System.getProperty("spring.data.redis.timeout", "2000"))
                    ));

            if (sslEnabled) {
                builder.useSsl()
                       .disablePeerVerification();
            }

			return new LettuceConnectionFactory(config, builder.build());
    }
    
    // @Bean
    // public LettuceConnectionFactory redisConnectionFactory() {
    // 	RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
    //     config.setPassword(password);
    //     return new LettuceConnectionFactory(config);
    // }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        
        // 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        return template;
    }
    
    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }
}

