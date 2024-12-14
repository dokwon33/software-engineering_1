package UOSense.UOSense_Backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    /** 이메일 인증용 */
    @Bean
    public RedisConnectionFactory redisConnectionFactoryDb0() {
        RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration(redisHost, redisPort);
        config.setDatabase(0);

        return new LettuceConnectionFactory(config);
    }

    @Bean(name = "stringRedisTemplateDb0")
    public StringRedisTemplate stringRedisTemplateDb0() {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactoryDb0());
        return stringRedisTemplate;
    }

    /** refresh token 저장용 */
    @Bean
    public RedisConnectionFactory redisConnectionFactoryDb1() {
        RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration(redisHost, redisPort);
        config.setDatabase(1);

        return new LettuceConnectionFactory(config);
    }

    @Bean(name = "redisTemplate")
    public StringRedisTemplate stringRedisTemplateDb1() {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactoryDb1());
        return stringRedisTemplate;
    }
}