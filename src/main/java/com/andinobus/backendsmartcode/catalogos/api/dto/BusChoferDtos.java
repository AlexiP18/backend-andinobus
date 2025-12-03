package com.andinobus.backendsmartcode.catalogos.api.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class BusChoferDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusChoferResponse {
        private Long id;
        private Long busId;
        private String busPlaca;
        private String busNumeroInterno;
        private Long choferId;
        private String choferNombre;
        private String choferCedula;
        private String choferTelefono;
        private String choferFotoUrl;
        private String tipo; // PRINCIPAL, ALTERNO
        private Integer orden;
        private Boolean activo;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AsignarChoferRequest {
        private Long choferId;
        private String tipo; // PRINCIPAL, ALTERNO
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SincronizarChoferesRequest {
        private List<ChoferAsignacion> choferes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChoferAsignacion {
        private Long choferId;
        private String tipo; // PRINCIPAL, ALTERNO
        private Integer orden;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChoferDisponible {
        private Long id;
        private String nombres;
        private String apellidos;
        private String nombreCompleto;
        private String cedula;
        private String telefono;
        private String email;
        private String fotoUrl;
        private String numeroLicencia;
        private String tipoLicencia;
        private String fechaVencimientoLicencia;
        private Boolean yaAsignado;
        private String busAsignadoPlaca;
    }
}
