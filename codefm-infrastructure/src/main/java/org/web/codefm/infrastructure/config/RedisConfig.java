package org.web.codefm.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
        final GenericJackson2JsonRedisSerializer serializer = this.buildSerializer();
        final RedisCacheConfiguration defaultConfig = this.buildCacheConfig(this.timeToLive, serializer);

        final Map<String, RedisCacheConfiguration> cacheConfigurations = this.ttl.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> this.buildCacheConfig(entry.getValue(), serializer)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    private GenericJackson2JsonRedisSerializer buildSerializer() {
        final PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    private RedisCacheConfiguration buildCacheConfig(long ttlMillis, GenericJackson2JsonRedisSerializer serializer) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(ttlMillis))
                .prefixCacheNameWith(this.keyPrefix)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }
}
