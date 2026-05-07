package br.com.novaalianca.mnss.onlineapp.config;

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

    public static final String CACHE_PUBLIC_MENU      = "public_menu";
    public static final String CACHE_STORE_INFO       = "store_info";
    public static final String CACHE_ONLINE_PRODUCTS  = "online_products";

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> builder
                .withCacheConfiguration(CACHE_PUBLIC_MENU,     cacheConfig(Duration.ofMinutes(2)))
                .withCacheConfiguration(CACHE_STORE_INFO,      cacheConfig(Duration.ofHours(1)))
                .withCacheConfiguration(CACHE_ONLINE_PRODUCTS, cacheConfig(Duration.ofMinutes(5)));
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
