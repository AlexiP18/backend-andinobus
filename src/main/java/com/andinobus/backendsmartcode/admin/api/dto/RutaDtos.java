package com.andinobus.backendsmartcode.admin.api.dto;

import lombok.*;

import java.time.LocalDate;

public class RutaDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RutaResponse {
        private Long id;
        private String nombre;
        private String origen;
        private String destino;
        private Double distanciaKm;
        private Integer duracionEstimadaMinutos;
        private String descripcion;
        private Boolean aprobadaAnt;
        private String numeroResolucionAnt;
        private LocalDate fechaAprobacionAnt;
        private LocalDate vigenciaHasta;
        private String observacionesAnt;
        private Boolean activo;
        private String tipoRuta; // INTERPROVINCIAL o INTRAPROVINCIAL
        private Long terminalOrigenId;
        private Long terminalDestinoId;
        private String terminalOrigenNombre;
        private String terminalDestinoNombre;
        private Integer cantidadCaminos; // Cantidad de caminos asociados a la ruta
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRutaRequest {
        private String nombre;
        private String origen;
        private String destino;
        private Double distanciaKm;
        private Integer duracionEstimadaMinutos;
        private String descripcion;
        private Boolean aprobadaAnt;
        private String numeroResolucionAnt;
        private LocalDate fechaAprobacionAnt;
        private LocalDate vigenciaHasta;
        private String observacionesAnt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRutaRequest {
        private String nombre;
        private String origen;
        private String destino;
        private Double distanciaKm;
        private Integer duracionEstimadaMinutos;
        private String descripcion;
        private Boolean aprobadaAnt;
        private String numeroResolucionAnt;
        private LocalDate fechaAprobacionAnt;
        private LocalDate vigenciaHasta;
        private String observacionesAnt;
        private Boolean activo;
    }
}
