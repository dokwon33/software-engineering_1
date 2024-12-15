package UOSense.UOSense_Backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class SwaggerConfig implements WebMvcConfigurer {

    private static final String SERVICE_NAME = "PROJECT UOSENSE";
    private static final String API_VERSION = "V1";
    private static final String API_DESCRIPTION = "PROJECT UOSENSE API TEST";
    private static final String API_URL = "http://localhost:8080/";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("PROJECT UOSENSE")
                .description("### 시대생을 위한 맛집 탐방 지도 서비스")
                .version("1.0.0");
    }
}
