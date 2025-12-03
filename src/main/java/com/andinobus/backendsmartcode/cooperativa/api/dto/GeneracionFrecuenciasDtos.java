package com.andinobus.backendsmartcode.cooperativa.api.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

public class GeneracionFrecuenciasDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TurnoFrecuencia {
        private Integer numeroDia; // DIA en el CSV (1-36)
        private String horaSalida;
        private String origen;
        private String destino;
        private String horaLlegada;
        private Boolean esParada; // Si el bus está en parada
        private List<SubTurno> subTurnos; // Para días con múltiples rutas
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubTurno {
        private String horaSalida;
        private String origen;
        private String destino;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlantillaRotacion {
        private Long id;
        private Long cooperativaId;
        private String nombre;
        private String descripcion;
        private Integer totalTurnos; // Total de DIAs en la rotación (ej: 36)
        private Integer totalBuses; // Cantidad de buses que participan
        private List<TurnoFrecuencia> turnos;
        private Boolean activa;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CrearPlantillaRequest {
        private String nombre;
        private String descripcion;
        private List<TurnoFrecuencia> turnos;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerarFrecuenciasRequest {
        private Long plantillaId; // Usar plantilla existente
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private List<Long> busIds; // Buses que participan en la rotación
        private Boolean asignarChoferesAutomaticamente;
        private Boolean sobreescribirExistentes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AsignacionBusDia {
        private LocalDate fecha;
        private Long busId;
        private String busPlaca;
        private Integer turnoAsignado; // DIA del turno
        private String primerViaje; // Ej: "AMBATO → QUITO 16:00"
        private List<ViajeGenerado> viajes;
        private Boolean esParada;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViajeGenerado {
        private String origen;
        private String destino;
        private String horaSalida;
        private String horaLlegadaEstimada;
        private Long choferId;
        private String choferNombre;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreviewGeneracionResponse {
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private Integer diasTotales;
        private Integer frecuenciasAGenerar;
        private Integer busesParticipantes;
        private List<AsignacionBusDia> asignaciones;
        private List<String> advertencias;
        private List<ConflictoDetectado> conflictos;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictoDetectado {
        private LocalDate fecha;
        private Long busId;
        private String busPlaca;
        private String descripcion;
        private String tipoConflicto; // CHOFER_SIN_HORAS, BUS_NO_DISPONIBLE, FRECUENCIA_EXISTENTE
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultadoGeneracionResponse {
        private Integer frecuenciasCreadas;
        private Integer frecuenciasOmitidas;
        private Integer errores;
        private List<String> mensajes;
        private List<FrecuenciaGeneradaInfo> frecuenciasGeneradas;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrecuenciaGeneradaInfo {
        private Long frecuenciaId;
        private LocalDate fecha;
        private String origen;
        private String destino;
        private String horaSalida;
        private Long busId;
        private String busPlaca;
        private Long choferId;
        private String choferNombre;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportarCsvRequest {
        private String contenidoCsv;
        private String nombrePlantilla;
        private String descripcion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportarCsvResponse {
        private Boolean exitoso;
        private Long plantillaId;
        private Integer turnosImportados;
        private List<String> errores;
        private List<String> advertencias;
    }
}
