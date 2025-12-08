package com.andinobus.backendsmartcode.cooperativa.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PersonalDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePersonalRequest {
        private Long cooperativaId;
        private String nombres;
        private String apellidos;
        private String email;
        private String password;
        private String cedula;
        private String telefono;
        private String rolCooperativa; // ADMIN | OFICINISTA | CHOFER
        // Campos adicionales para CHOFER
        private String numeroLicencia;
        private String tipoLicencia;
        private String fechaVencimientoLicencia; // YYYY-MM-DD
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatePersonalRequest {
        private String nombres;
        private String apellidos;
        private String email;
        private String cedula;
        private String telefono;
        private String rolCooperativa;
        private Boolean activo;
        // Campos adicionales para CHOFER
        private String numeroLicencia;
        private String tipoLicencia;
        private String fechaVencimientoLicencia; // YYYY-MM-DD
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalResponse {
        private Long id;
        private Long cooperativaId;
        private String cooperativaNombre;
        private String nombres;
        private String apellidos;
        private String email;
        private String cedula;
        private String telefono;
        private String rolCooperativa;
        private Boolean activo;
        private String fotoUrl;
        // Campos adicionales para CHOFER
        private String numeroLicencia;
        private String tipoLicencia;
        private String fechaVencimientoLicencia; // YYYY-MM-DD
        // Bus asignado (solo para CHOFER)
        private Long busAsignadoId;
        private String busAsignadoPlaca;
        private String busAsignadoNumeroInterno;
    }
}

