package com.andinobus.backendsmartcode.catalogos.api.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class CooperativaTerminalDtos {

    // ==================== RESPONSE DTOs ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CooperativaTerminalResponse {
        private Long id;
        private Long cooperativaId;
        private String cooperativaNombre;
        private Long terminalId;
        private String terminalNombre;
        private String terminalCanton;
        private String terminalProvincia;
        private String terminalTipologia;
        private Boolean esSedePrincipal;
        private Integer numeroAndenesAsignados;
        private String observaciones;
        private Boolean activo;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TerminalAsignadoResponse {
        private Long terminalId;
        private String nombre;
        private String canton;
        private String provincia;
        private String tipologia;
        private Boolean esSedePrincipal;
        private Integer numeroAndenesAsignados;
    }

    // ==================== REQUEST DTOs ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AsignarTerminalRequest {
        private Long terminalId;
        private Boolean esSedePrincipal;
        private Integer numeroAndenesAsignados;
        private String observaciones;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AsignarTerminalesRequest {
        private List<Long> terminalIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateCooperativaTerminalRequest {
        private Boolean esSedePrincipal;
        private Integer numeroAndenesAsignados;
        private String observaciones;
    }
}
