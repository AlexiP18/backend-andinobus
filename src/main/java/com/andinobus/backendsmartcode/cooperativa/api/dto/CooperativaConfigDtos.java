package com.andinobus.backendsmartcode.cooperativa.api.dto;

import lombok.*;

public class CooperativaConfigDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConfiguracionResponse {
        private Long id;
        private String nombre;
        private String ruc;
        private String logoUrl;
        private String descripcion;
        private String colorPrimario;
        private String colorSecundario;
        private String facebook;
        private String twitter;
        private String instagram;
        private String linkedin;
        private String youtube;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateConfiguracionRequest {
        private String nombre;
        private String descripcion;
        private String logoUrl;
        private String colorPrimario;
        private String colorSecundario;
        private String facebook;
        private String twitter;
        private String instagram;
        private String linkedin;
        private String youtube;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateLogoRequest {
        private String logoBase64;
        private String fileName;
    }
}
