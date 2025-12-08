package com.andinobus.backendsmartcode.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOriginsProperty;

    /**
     * Filtro CORS con la más alta prioridad para asegurar que los preflight requests
     * se manejen correctamente antes de cualquier otro filtro.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Parsear orígenes permitidos
        List<String> origins = Arrays.stream(allowedOriginsProperty.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        
        // Si no hay orígenes configurados, permitir el frontend por defecto
        if (origins.isEmpty()) {
            origins = List.of("https://frontandinobus-production.up.railway.app", "http://localhost:3000");
        }
        
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(Arrays.asList("Location", "Content-Disposition", "Authorization"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
