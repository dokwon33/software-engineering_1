package UOSense.UOSense_Backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        var cacheManager = new org.springframework.cache.caffeine.CaffeineCacheManager("restaurantCache");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES) // 캐시 만료 시간 설정
                .maximumSize(100)); // 캐시에 저장할 최대 엔트리 수 설정
        return cacheManager;
    }
}