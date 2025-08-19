package com.matchFit.common.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

@Configuration
public class RedisConfig {
	
    @Value("${spring.data.redis.host}")
    private String host;
    
    @Value("${spring.data.redis.port}")
    private int port;
    
    @Value("${spring.data.redis.password}") 
    String password;

    

    // @Bean
    // public LettuceConnectionFactory redisConnectionFactory() {
    // 	RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
    //     config.setPassword(password);
    //     return new LettuceConnectionFactory(config);
    // }

 //    @Value("${spring.data.redis.ssl:false}")
 //    private boolean sslEnabled;
    
 //    private final RedisProperties redisProperties;
	
	// public RedisConfig(RedisProperties redisProperties) {
 //        this.redisProperties = redisProperties;
 //    }
    
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
    	
    	
    	// 1) Standalone 설정
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(host, port);
        // standaloneConfig.setHostName(redisProperties.getHost());
        // standaloneConfig.setPort(redisProperties.getPort());
        // if (StringUtils.hasText(redisProperties.getPassword())) {
        //     standaloneConfig.setPassword(RedisPassword.of(redisProperties.getPassword()));
        // }
        standaloneConfig.setPassword(RedisPassword.of(password));

	
        // 2) Lettuce 클라이언트 설정 분기
//        LettuceClientConfiguration.LettuceSslClientConfigurationBuilder clientBuilder =
//        LettuceClientConfiguration.builder().useSsl();	
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .build();


        // if (sslEnabled) {
        //     clientBuilder
        //         .useSsl()                    // SSL/TLS 사용
        //         .disablePeerVerification();  // 인증서 검증 끌 때 (필요 시)
        // }

//        LettuceClientConfiguration clientConfig = clientBuilder.build();

      

        // 3) 팩토리 생성
        return new LettuceConnectionFactory(standaloneConfig, clientConfig);
    }

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
