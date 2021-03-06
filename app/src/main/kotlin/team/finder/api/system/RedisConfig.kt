package team.finder.api.system

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
import java.time.Duration


@Configuration
class RedisConfig {

    @Bean
    fun cacheConfiguration(): RedisCacheConfiguration {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(3))
            .disableCachingNullValues()
            .serializeValuesWith(SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer()))
    }

}
