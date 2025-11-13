package com.andinobus.backendsmartcode.operacion.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
public class HojaRutaDtos {

    @Data
    public static class GenerarRequest {
        /** Fecha en formato YYYY-MM-DD */
        @NotBlank
        private String fecha;
        private Long cooperativaId; // opcional
        /** manual | auto */
        @NotBlank
        private String modo;
    }

    @Data
    @Builder
    public static class GenerarResponse {
        private String fecha;
        private Long cooperativaId;
        private String modo;
        private Integer viajesGenerados;
        private String status; // e.g., accepted
        private String message;
    }
}
