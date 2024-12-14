package UOSense.UOSense_Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class UoSenseBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(UoSenseBackendApplication.class, args);
    }

}
