package UOSense.UOSense_Backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")   // 서버의 /api/v1/ 경로에 대해 CORS 정책을 적용
                .allowedOrigins("http://localhost:3000")    // 허용할 출처
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")   // 허용할 HTTP method
                .allowCredentials(true); // 쿠키 인증 요청
    }
}
