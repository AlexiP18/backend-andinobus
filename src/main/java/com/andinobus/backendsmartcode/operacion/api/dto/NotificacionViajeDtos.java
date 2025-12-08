package com.andinobus.backendsmartcode.operacion.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class NotificacionViajeDtos {

    /**
     * DTO de respuesta para una notificación
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificacionResponse {
        private Long id;
        private Long viajeId;
        private String tipo; // VIAJE_INICIADO, VIAJE_FINALIZADO, VIAJE_CANCELADO, ALERTA_RETRASO
        private String titulo;
        private String mensaje;
        private String detalleViaje;
        private Boolean leida;
        private LocalDateTime fechaCreacion;
        private LocalDateTime fechaLectura;
    }

    /**
     * DTO para el conteo de notificaciones no leídas
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificacionCountResponse {
        private Long noLeidas;
    }

    /**
     * DTO de respuesta para operación de marcado
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MarcadoResponse {
        private Integer notificacionesMarcadas;
        private String mensaje;
    }
}
