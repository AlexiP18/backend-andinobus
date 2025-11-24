package com.andinobus.backendsmartcode.admin.api.dto;

import lombok.*;

public class ConfiguracionGlobalDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConfiguracionGlobalResponse {
        private Long id;
        private String nombreAplicacion;
        private String logoUrl;
        private String logoSmallUrl;
        private String faviconUrl;
        private String colorPrimario;
        private String colorSecundario;
        private String colorAcento;
        private String facebookUrl;
        private String twitterUrl;
        private String instagramUrl;
        private String youtubeUrl;
        private String linkedinUrl;
        private String emailSoporte;
        private String telefonoSoporte;
        private String whatsappSoporte;
        private String direccionFisica;
        private String horarioAtencion;
        private String sitioWeb;
        private String terminosCondicionesUrl;
        private String politicaPrivacidadUrl;
        private String descripcion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateConfiguracionRequest {
        private String nombreAplicacion;
        private String logoUrl;
        private String logoSmallUrl;
        private String faviconUrl;
        private String colorPrimario;
        private String colorSecundario;
        private String colorAcento;
        private String facebookUrl;
        private String twitterUrl;
        private String instagramUrl;
        private String youtubeUrl;
        private String linkedinUrl;
        private String emailSoporte;
        private String telefonoSoporte;
        private String whatsappSoporte;
        private String direccionFisica;
        private String horarioAtencion;
        private String sitioWeb;
        private String terminosCondicionesUrl;
        private String politicaPrivacidadUrl;
        private String descripcion;
    }
}
