package NytePulse.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")               // Apply to all endpoints
                .allowedOriginPatterns("*")      // Allow requests from any file or website
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow these HTTP verbs
                .allowedHeaders("*")             // Allow all headers (like Authorization, User-Id)
                .allowCredentials(true);
    }
}