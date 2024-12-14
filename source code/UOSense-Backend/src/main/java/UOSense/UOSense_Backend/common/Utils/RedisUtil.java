package UOSense.UOSense_Backend.common.Utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;

@RequiredArgsConstructor
@Component
public class RedisUtil {
    /** 구분하고 싶을 경우 @Qualifier("stringRedisTemplateDb0") 이런 식으로 달기*/
    private final StringRedisTemplate redisTemplate;
    @Value("${spring.mail.properties.auth-code-expiration-millis}")
    private long DURANTION;
    
    /**  key를 통해 value(데이터)를 얻는다.*/
    public String getData(String key) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    /** duration 동안 (key, value)를 저장한다.*/
    public void setDataExpire(String key, String value) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration = Duration.ofMillis(DURANTION);
        valueOperations.set(key, value, expireDuration);
    }

    /** 데이터 삭제*/
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }
}
