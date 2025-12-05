package com.andinobus.backendsmartcode.admin.application.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

public class FrecuenciaDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FrecuenciaViajeResponse {
        private Long id;
        private Long busId;
        private String busPlaca;
        private Long rutaId;
        private String rutaNombre;
        private String rutaOrigen;
        private String rutaDestino;

        @JsonFormat(pattern = "HH:mm")
        private LocalTime horaSalida;

        @JsonFormat(pattern = "HH:mm")
        private LocalTime horaLlegadaEstimada;

        private String diasOperacion;
        private Double precioBase;
        private Integer asientosDisponibles;
        private String observaciones;
        private Boolean activo;
        private List<ParadaResponse> paradas;
        
        // Nuevos campos para tipo de frecuencia y rotación
        private String tipoFrecuencia; // INTERPROVINCIAL, INTRAPROVINCIAL
        private Integer tiempoMinimoEsperaMinutos;
        private Boolean requiereBusEnTerminal;
        private Long terminalOrigenId;
        private String terminalOrigenNombre;
        private Long terminalDestinoId;
        private String terminalDestinoNombre;
        private String estado;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParadaResponse {
        private Long id;
        private Integer orden;
        private String nombreParada;
        private String direccion;

        @JsonFormat(pattern = "HH:mm")
        private LocalTime tiempoLlegada;

        private Integer tiempoEsperaMinutos;
        private Double precioDesdeOrigen;
        private String observaciones;
        private Boolean permiteAbordaje;
        private Boolean permiteDescenso;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateFrecuenciaRequest {
        private Long busId;
        private Long rutaId;

        @JsonFormat(pattern = "HH:mm")
        private LocalTime horaSalida;

        @JsonFormat(pattern = "HH:mm")
        private LocalTime horaLlegadaEstimada;

        private String diasOperacion; // CSV: "LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,DOMINGO"
        private Double precioBase;
        private Integer asientosDisponibles;
        private String observaciones;
        private List<CreateParadaRequest> paradas;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateParadaRequest {
        private Integer orden;
        private String nombreParada;
        private String direccion;

        @JsonFormat(pattern = "HH:mm")
        private LocalTime tiempoLlegada;

        private Integer tiempoEsperaMinutos;
        private Double precioDesdeOrigen;
        private String observaciones;
        private Boolean permiteAbordaje;
        private Boolean permiteDescenso;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateFrecuenciaRequest {
        @JsonFormat(pattern = "HH:mm")
        private LocalTime horaSalida;

        @JsonFormat(pattern = "HH:mm")
        private LocalTime horaLlegadaEstimada;

        private String diasOperacion;
        private Double precioBase;
        private Integer asientosDisponibles;
        private String observaciones;
        private List<CreateParadaRequest> paradas;
    }
}
