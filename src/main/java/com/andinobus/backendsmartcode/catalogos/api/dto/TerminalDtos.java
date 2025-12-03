package com.andinobus.backendsmartcode.catalogos.api.dto;

import lombok.*;

public class TerminalDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TerminalResponse {
        private Long id;
        private String nombre;
        private String provincia;
        private String canton;
        private String tipologia;
        private String descripcionTipologia;
        private Integer andenes;
        private Integer frecuenciasPorAnden;
        private Integer maxFrecuenciasDiarias;
        private Double latitud;
        private Double longitud;
        private String direccion;
        private String telefono;
        private String horarioApertura;
        private String horarioCierre;
        private String imagenUrl;
        private Boolean activo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TerminalCreateRequest {
        private String nombre;
        private String provincia;
        private String canton;
        private String tipologia;
        private Integer andenes;
        private Integer frecuenciasPorAnden;
        private Double latitud;
        private Double longitud;
        private String direccion;
        private String telefono;
        private String horarioApertura;
        private String horarioCierre;
        private String imagenUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TerminalUpdateRequest {
        private String nombre;
        private String provincia;
        private String canton;
        private String tipologia;
        private Integer andenes;
        private Integer frecuenciasPorAnden;
        private Double latitud;
        private Double longitud;
        private String direccion;
        private String telefono;
        private String horarioApertura;
        private String horarioCierre;
        private String imagenUrl;
        private Boolean activo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TerminalStatsResponse {
        private long totalTerminales;
        private long terminalesT1;
        private long terminalesT2;
        private long terminalesT3;
        private long terminalesT4;
        private long terminalesT5;
        private long capacidadTotalFrecuencias;
    }
}
