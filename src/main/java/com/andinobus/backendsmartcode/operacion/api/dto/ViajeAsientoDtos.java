package com.andinobus.backendsmartcode.operacion.api.dto;

import lombok.Builder;
import lombok.Data;

public class ViajeAsientoDtos {

    @Data
    @Builder
    public static class AsientoDisponibilidadResponse {
        private String numeroAsiento; // "1", "2", "3", etc.
        private String tipoAsiento; // "NORMAL" | "VIP" | "ACONDICIONADO"
        private String estado; // "DISPONIBLE" | "RESERVADO" | "VENDIDO" | "BLOQUEADO"
    }

    @Data
    @Builder
    public static class AsientosEstadisticasResponse {
        private long total;
        private long disponibles;
        private long reservados;
        private long vendidos;
    }

    @Data
    @Builder
    public static class InicializarAsientosResponse {
        private boolean success;
        private String mensaje;
        private int asientosCreados;
    }
}
