package com.andinobus.backendsmartcode.operacion.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ViajeDtos {

    /**
     * DTO para listar viajes disponibles para venta
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ViajeDisponibleResponse {
        private Long id;
        private String origen;
        private String destino;
        private LocalDate fecha;
        private LocalTime horaSalida;
        private String busPlaca;
        private Integer capacidadTotal;
        private Integer asientosDisponibles;
        private BigDecimal precioBase;
        private String estado;
    }

    /**
     * DTO para detalle de un viaje con asientos
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ViajeDetalleResponse {
        private Long id;
        private String origen;
        private String destino;
        private LocalDate fecha;
        private LocalTime horaSalida;
        private LocalTime horaLlegadaEstimada;
        private String busPlaca;
        private String busMarca;
        private Integer capacidadTotal;
        private Integer asientosDisponibles;
        private BigDecimal precioBase;
        private String estado;
        private List<AsientoResponse> asientos;
    }

    /**
     * DTO para información de un asiento
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AsientoResponse {
        private Long id;
        private String numeroAsiento;
        private String tipoAsiento;
        private String estado;
        private Long reservaId;
    }
}
