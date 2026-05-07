package br.com.novaalianca.mnss.localapp.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfiguration {

    public static final String CACHE_PRODUCTS   = "products";
    public static final String CACHE_CATEGORIES = "categories";
    public static final String CACHE_STOCK      = "stock-balance";
    public static final String CACHE_STORE_INFO = "store-info";

    @Bean
    public RedisCacheManagerBuilderCustomizer cacheManagerCustomizer() {
        return builder -> builder
                .withCacheConfiguration(CACHE_PRODUCTS,  cacheConfig(Duration.ofMinutes(5)))
                .withCacheConfiguration(CACHE_CATEGORIES, cacheConfig(Duration.ofMinutes(10)))
                .withCacheConfiguration(CACHE_STOCK,     cacheConfig(Duration.ofSeconds(30)))
                .withCacheConfiguration(CACHE_STORE_INFO, cacheConfig(Duration.ofHours(1)));
    }

    private org.springframework.data.redis.cache.RedisCacheConfiguration cacheConfig(Duration ttl) {
        return org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()));
    }
}
