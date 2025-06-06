package tqs.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:80", "http://localhost", "http://localhost:3000")
                .allowedMethods("*") // or specifically: GET, POST, PUT, DELETE, OPTIONS
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
