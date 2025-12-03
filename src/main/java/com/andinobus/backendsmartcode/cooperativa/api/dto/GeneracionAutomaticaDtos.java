package com.andinobus.backendsmartcode.cooperativa.api.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTOs para la generación automática de frecuencias basada en reglas de negocio
 */
public class GeneracionAutomaticaDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerarAutomaticoRequest {
        private Long terminalOrigenId;      // Opcional - si no se envía, usa todas las rutas
        private Long terminalDestinoId;      // Opcional - si no se envía, usa todas las rutas
        private List<RutaSeleccionada> rutasSeleccionadas; // Lista de rutas específicas (opcional)
        private Boolean generarTodasLasRutas; // Si es true, genera para todas las rutas disponibles
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private List<String> diasOperacion; // LUNES, MARTES, etc.
        private LocalTime horaInicio;
        private LocalTime horaFin;
        private Integer intervaloMinutos; // Intervalo entre frecuencias (ej: 30 min)
        private Double precioBase;
        private Integer duracionViajeMinutos;
        private Boolean asignarChoferesAutomaticamente;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RutaSeleccionada {
        private Long terminalOrigenId;
        private Long terminalDestinoId;
        private Double precioBase;
        private Integer duracionMinutos;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreviewAutomaticoResponse {
        private Integer totalFrecuencias;
        private Integer frecuenciasPorDia;
        private Integer diasOperacion;
        private Integer busesNecesarios;
        private Integer busesDisponibles;
        private Boolean tieneCapacidadSuficiente;
        private List<FrecuenciaPrevisualizacion> frecuencias;
        private List<String> advertencias;
        private List<String> errores;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrecuenciaPrevisualizacion {
        private LocalDate fecha;
        private String diaSemana;
        private LocalTime horaSalida;
        private LocalTime horaLlegada;
        private String origen;
        private String destino;
        private Long busId;
        private String busPlaca;
        private Long choferId;
        private String choferNombre;
        private Double precio;
        private String estado; // OK, SIN_BUS, SIN_CHOFER
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultadoGeneracionAutomatica {
        private Integer frecuenciasCreadas;
        private Integer frecuenciasConAdvertencias;
        private Integer errores;
        private List<FrecuenciaCreada> frecuenciasGeneradas;
        private List<String> mensajes;
        private List<String> advertencias;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrecuenciaCreada {
        private Long frecuenciaId;
        private LocalDate fecha;
        private LocalTime horaSalida;
        private String ruta;
        private String busPlaca;
        private String choferNombre;
        private Double precio;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadoGeneracion {
        private Integer busesTotales;
        private Integer busesDisponibles;
        private Integer choferesTotales;
        private Integer choferesDisponibles;
        private List<RutaDisponible> rutasDisponibles;
        private ConfiguracionActual configuracion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RutaDisponible {
        private Long terminalOrigenId;
        private String terminalOrigenNombre;
        private Long terminalDestinoId;
        private String terminalDestinoNombre;
        private Double distanciaKm;
        private Integer duracionEstimadaMinutos;
        private Double precioSugerido;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfiguracionActual {
        private Integer maxHorasChofer;
        private Integer maxHorasExcepcionales;
        private Integer maxDiasExcepcionales;
        private Integer tiempoDescansoMinutos;
        private Integer intervaloMinimoFrecuencias;
        private String horaInicio;
        private String horaFin;
    }
}
