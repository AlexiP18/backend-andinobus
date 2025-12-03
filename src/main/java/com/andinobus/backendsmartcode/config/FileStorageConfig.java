package com.andinobus.backendsmartcode.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuración para servir archivos estáticos (fotos de perfil)
 * y crear directorios necesarios al iniciar la aplicación
 */
@Slf4j
@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    private static final String[] REQUIRED_DIRECTORIES = {
            "uploads/personal/fotos",
            "uploads/clientes/fotos",
            "uploads/buses/fotos",
            "uploads/logos"
    };

    /**
     * Crea los directorios necesarios para almacenar archivos
     * al iniciar la aplicación.
     */
    @PostConstruct
    public void initializeStorageDirectories() {
        log.info("Inicializando directorios de almacenamiento de archivos...");
        
        for (String directory : REQUIRED_DIRECTORIES) {
            Path path = Paths.get(directory);
            try {
                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                    log.info("✓ Directorio creado: {}", path.toAbsolutePath());
                } else {
                    log.debug("✓ Directorio ya existe: {}", path.toAbsolutePath());
                }
            } catch (IOException e) {
                log.error("✗ Error al crear directorio {}: {}", directory, e.getMessage());
                throw new RuntimeException("No se pudo crear el directorio " + directory, e);
            }
        }
        
        log.info("Directorios de almacenamiento inicializados correctamente");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir archivos de fotos de personal
        registry.addResourceHandler("/uploads/personal/fotos/**")
                .addResourceLocations("file:uploads/personal/fotos/")
                .setCachePeriod(3600); // Cache por 1 hora

        // Servir archivos de fotos de clientes (para uso futuro)
        registry.addResourceHandler("/uploads/clientes/fotos/**")
                .addResourceLocations("file:uploads/clientes/fotos/")
                .setCachePeriod(3600);

        // Servir archivos de fotos de buses (para uso futuro)
        registry.addResourceHandler("/uploads/buses/fotos/**")
                .addResourceLocations("file:uploads/buses/fotos/")
                .setCachePeriod(3600);

        // Servir archivos de logos de cooperativas
        registry.addResourceHandler("/uploads/logos/**")
                .addResourceLocations("file:uploads/logos/")
                .setCachePeriod(3600);
    }
}
