package UOSense.UOSense_Backend.common.Utils;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisUtilForToken {
    /** 구분하고 싶을 경우 @Qualifier("stringRedisTemplateDb0") 이런 식으로 달기*/
    private final StringRedisTemplate redisTemplate;

    public RedisUtilForToken(@Qualifier("redisTemplate") StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    /** duration 동안 (key, value)를 저장한다.*/
    public void setToken(String key, String data, Duration duration) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, data, duration);
    }

    /**  key를 통해 value(데이터)를 얻는다.*/
    public String getToken(String key) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    /** 데이터 삭제*/
    public void deleteToken(String key) {
        redisTemplate.delete(key);
    }

    public boolean checkExistsToken(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}