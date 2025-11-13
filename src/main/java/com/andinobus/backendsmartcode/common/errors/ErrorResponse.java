package com.andinobus.backendsmartcode.common.errors;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Modelo estándar de respuesta de error para toda la API.
 * Mantiene consistencia con lo documentado en API.md.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        List<Detail> details,
        String path
) {
    public static ErrorResponse of(int status, String code, String message, List<Detail> details, String path) {
        return new ErrorResponse(Instant.now(), status, code, message,
                details == null ? Collections.emptyList() : details, path);
    }

    public static ErrorResponse of(int status, String code, String message, String path) {
        return of(status, code, message, Collections.emptyList(), path);
    }

    /**
     * Elemento de detalle para errores de validación u otros campos.
     */
    public record Detail(String field, String message) { }
}
