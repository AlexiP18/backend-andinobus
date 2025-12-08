package com.andinobus.backendsmartcode.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuración para habilitar operaciones asíncronas
 * Usado principalmente para envío de emails sin bloquear la respuesta
 */
@Configuration
@Profile("dev")
@EnableAsync
public class AsyncConfig {
    // La configuración por defecto de Spring es suficiente para nuestro caso
    // Si necesitas personalizar el thread pool, puedes agregar un @Bean Executor aquí
}
