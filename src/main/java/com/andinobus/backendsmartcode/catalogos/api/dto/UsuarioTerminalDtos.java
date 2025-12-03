package com.andinobus.backendsmartcode.catalogos.api.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class UsuarioTerminalDtos {

    // ==================== RESPONSE DTOs ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UsuarioTerminalResponse {
        private Long id;
        private Long usuarioId;
        private String usuarioNombre;
        private String usuarioEmail;
        private Long terminalId;
        private String terminalNombre;
        private String terminalCanton;
        private String terminalProvincia;
        private Long cooperativaId;
        private String cooperativaNombre;
        private String cargo;
        private String turno;
        private Boolean activo;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TerminalAsignadoUsuarioResponse {
        private Long terminalId;
        private String nombre;
        private String canton;
        private String provincia;
        private String tipologia;
        private String cargo;
        private String turno;
        private Long cooperativaId;
        private String cooperativaNombre;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OficinistaPorTerminalResponse {
        private Long usuarioId;
        private String nombres;
        private String apellidos;
        private String email;
        private String cargo;
        private String turno;
        private Long cooperativaId;
        private String cooperativaNombre;
    }

    // ==================== REQUEST DTOs ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AsignarTerminalUsuarioRequest {
        private Long terminalId;
        private Long cooperativaId;
        private String cargo;
        private String turno; // MAÑANA, TARDE, NOCHE, COMPLETO
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AsignarTerminalesUsuarioRequest {
        private List<Long> terminalIds;
        private Long cooperativaId;
        private String cargo;
        private String turno;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateUsuarioTerminalRequest {
        private Long cooperativaId;
        private String cargo;
        private String turno;
    }
}
