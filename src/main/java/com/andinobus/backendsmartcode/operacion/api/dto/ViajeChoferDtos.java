package com.andinobus.backendsmartcode.operacion.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ViajeChoferDtos {

    /**
     * DTO para el viaje del chofer con lista de pasajeros
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ViajeChoferResponse {
        private Long id;
        private Long frecuenciaId; // ID de la frecuencia (cuando no hay viaje creado aún)
        private String origen;
        private String destino;
        private LocalDate fecha;
        private LocalTime horaSalidaProgramada;
        private LocalTime horaSalidaReal;
        private LocalTime horaLlegadaEstimada;
        private LocalTime horaLlegadaReal;
        private String busPlaca;
        private String busMarca;
        private Integer capacidadTotal;
        private Integer capacidadPiso1; // Capacidad del primer piso
        private Integer capacidadPiso2; // Capacidad del segundo piso (0 si es bus de un piso)
        private String estado; // PROGRAMADO | EN_RUTA | COMPLETADO
        private List<PasajeroViaje> pasajeros;
        private Integer totalPasajeros;
        private Integer pasajerosVerificados;
        
        // Información de coordenadas para el mapa
        private CoordenadaDTO coordenadaOrigen;
        private CoordenadaDTO coordenadaDestino;
        
        // Información de la cooperativa para notificaciones
        private Long cooperativaId;
        private String cooperativaNombre;
    }
    
    /**
     * DTO para coordenadas GPS
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CoordenadaDTO {
        private Double latitud;
        private Double longitud;
        private String nombreTerminal;
        private String canton;
        private String provincia;
    }

    /**
     * DTO para información de un pasajero del viaje
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PasajeroViaje {
        private Long reservaId;
        private String clienteEmail;
        private List<String> asientos;
        private String estado; // PAGADO | PENDIENTE
        private Boolean verificado; // Si el chofer verificó el abordaje
    }

    /**
     * DTO para iniciar un viaje
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IniciarViajeRequest {
        private LocalTime horaSalidaReal;
    }

    /**
     * DTO para finalizar un viaje
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinalizarViajeRequest {
        private LocalTime horaLlegadaReal;
        private String observaciones;
    }

    /**
     * DTO para verificar el abordaje de un pasajero
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerificarPasajeroRequest {
        private Long reservaId;
        private Boolean verificado;
    }

    /**
     * DTO de respuesta genérica
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ViajeOperacionResponse {
        private Long viajeId;
        private String estado;
        private String mensaje;
    }

    /**
     * DTO para historial de viajes completados
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ViajeHistorialResponse {
        private Long id;
        private String origen;
        private String destino;
        private LocalDate fecha;
        private LocalTime horaSalidaProgramada;
        private LocalTime horaSalidaReal;
        private LocalTime horaLlegadaEstimada;
        private LocalTime horaLlegadaReal;
        private String busPlaca;
        private Integer totalPasajeros;
        private Double promedioCalificacion;
        private Integer totalCalificaciones;
        private String observaciones;
    }

    /**
     * DTO para calificaciones de viaje
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalificacionResponse {
        private Long id;
        private Long viajeId;
        private String clienteEmail;
        private Integer puntuacion;
        private String comentario;
        private LocalDateTime fechaCalificacion;
        // Campos adicionales para contexto
        private String origen;
        private String destino;
        private LocalDate fechaViaje;
    }

    /**
     * DTO para resumen de calificaciones del chofer
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalificacionesChoferResponse {
        private List<CalificacionResponse> calificaciones;
        private Double promedioCalificacion;
        private Integer totalCalificaciones;
    }

    /**
     * DTO para las rutas asignadas al chofer
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RutaChoferResponse {
        private Long id;
        private String origen; // Cantón de origen
        private String destino; // Cantón de destino
        private String terminalOrigenNombre; // Nombre de la terminal de origen
        private String terminalDestinoNombre; // Nombre de la terminal de destino
        private LocalTime horaSalida;
        private Integer duracionEstimadaMin;
        private String diasOperacion;
        private Boolean activa;
        private Integer totalViajesRealizados; // Total de viajes que el chofer ha hecho en esta ruta
        private String busPlaca; // Placa del bus asignado a esta frecuencia
    }
}
