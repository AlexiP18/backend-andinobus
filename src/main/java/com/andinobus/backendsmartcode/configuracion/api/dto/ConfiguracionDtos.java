package com.andinobus.backendsmartcode.configuracion.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class ConfiguracionDtos {

    @Data
    public static class ConfiguracionRequest {
        private String logoUrl;
        private String colorPrimario;
        private String colorSecundario;
        private String facebook;
        private String instagram;
        private String soporteEmail;
        private String soporteTelefono;
    }

    @Data
    @Builder
    public static class ConfiguracionResponse {
        private String logoUrl;
        private String colorPrimario;
        private String colorSecundario;
        private String facebook;
        private String instagram;
        private String soporteEmail;
        private String soporteTelefono;
    }
}
