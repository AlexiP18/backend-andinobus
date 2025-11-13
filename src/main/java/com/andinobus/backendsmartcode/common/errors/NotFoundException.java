package com.andinobus.backendsmartcode.common.errors;

/**
 * Excepción para recursos no encontrados dentro de la API.
 * Puede ser lanzada desde servicios/controladores para devolver 404 con GlobalExceptionHandler.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super("Recurso no encontrado");
    }
    public NotFoundException(String message) {
        super(message);
    }
}
