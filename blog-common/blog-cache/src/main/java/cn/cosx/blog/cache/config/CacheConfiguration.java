package cn.cosx.blog.cache.config;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 缓存配置
 *
 * @author Hollis
 */
@Configuration
@EnableMethodCache(basePackages = "cn.cosx.blog")
public class CacheConfiguration {


        @Bean
        public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
            RedisTemplate<String, String> template = new RedisTemplate<>();
            template.setConnectionFactory(factory);

            StringRedisSerializer stringSerializer = new StringRedisSerializer();

            template.setKeySerializer(stringSerializer);
            template.setValueSerializer(stringSerializer);
            template.setHashKeySerializer(stringSerializer);
            template.setHashValueSerializer(stringSerializer);

            template.afterPropertiesSet();
            return template;
        }
}
