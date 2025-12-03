package com.andinobus.backendsmartcode.operacion.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para recibir actualizaciones de posición desde la app móvil del chofer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPosicionRequest {

    @NotNull(message = "La latitud es requerida")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private Double latitud;

    @NotNull(message = "La longitud es requerida")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private Double longitud;

    private Double velocidadKmh;

    private Double precision; // En metros

    @NotNull(message = "El timestamp es requerido")
    private LocalDateTime timestamp;

    private String provider; // GPS, NETWORK, FUSED
}
