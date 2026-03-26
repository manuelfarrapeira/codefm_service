package org.web.codefm.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
@ConfigurationProperties(prefix = "spring.cache.redis")
public class RedisConfig {

    private long timeToLive = 1800000;
    private String keyPrefix = "codefm::";
    private Map<String, Long> ttl = new HashMap<>();

    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public void setTtl(Map<String, Long> ttl) {
        this.ttl = ttl;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        final RedisCacheConfiguration defaultConfig = this.buildCacheConfig(this.timeToLive);

        final Map<String, RedisCacheConfiguration> cacheConfigurations = this.ttl.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> this.buildCacheConfig(entry.getValue())));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    private RedisCacheConfiguration buildCacheConfig(long ttlMillis) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(ttlMillis))
                .prefixCacheNameWith(this.keyPrefix)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
