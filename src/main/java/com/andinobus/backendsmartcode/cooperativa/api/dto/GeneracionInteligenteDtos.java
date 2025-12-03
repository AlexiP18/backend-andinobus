package com.andinobus.backendsmartcode.cooperativa.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * DTOs para la generación inteligente de frecuencias.
 * 
 * Reglas de negocio implementadas:
 * 1. Los buses operan en circuito (ida y vuelta)
 * 2. El bus debe estar físicamente en el terminal para salir
 * 3. Se respetan tiempos de descanso entre viajes
 * 4. Paradas intermedias solo en viajes interprovinciales
 */
public class GeneracionInteligenteDtos {

    // ============ Request ============
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerarInteligenteRequest {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate fechaInicio;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate fechaFin;
        
        private List<String> diasOperacion;           // LUNES, MARTES, etc.
        private List<RutaCircuitoRequest> rutasCircuito; // Rutas seleccionadas con configuración
        private Boolean permitirParadas;               // Habilitar paradas intermedias (solo interprov)
        private Integer maxParadasPersonalizado;       // Override del máximo de paradas (1-3)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RutaCircuitoRequest {
        private Long terminalOrigenId;
        private String terminalOrigenNombre;
        private Long terminalDestinoId;
        private String terminalDestinoNombre;
        private Double distanciaKm;
        private Integer duracionMinutos;
        private Double precioBase;
        private Boolean habilitarParadas;              // Habilitar paradas para esta ruta
        private Integer maxParadas;                    // Máx paradas para esta ruta (1-3)
        private List<Long> terminalesParadaIds;        // IDs de terminales intermedios para parada
    }

    // ============ Estado/Configuración ============

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadoGeneracionInteligente {
        private Integer busesDisponibles;
        private Integer choferesDisponibles;
        private Integer terminalesHabilitados;
        private List<RutaCircuito> rutasCircuito;
        private ConfiguracionGeneracion configuracion;
        private Integer capacidadEstimadaDiaria;       // Viajes estimados por día
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RutaCircuito {
        private Long terminalOrigenId;
        private String terminalOrigenNombre;
        private Long terminalDestinoId;
        private String terminalDestinoNombre;
        private Double distanciaKm;
        private Integer duracionMinutos;
        private String tipoFrecuencia;                 // INTERPROVINCIAL o INTRAPROVINCIAL
        private Integer maxParadasPermitidas;          // Según reglas de negocio
        private Double precioSugerido;
        private String provinciaOrigen;                // Provincia del terminal origen
        private String provinciaDestino;               // Provincia del terminal destino
        private String cantonOrigen;                   // Cantón del terminal origen
        private String cantonDestino;                  // Cantón del terminal destino
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfiguracionGeneracion {
        private Integer maxHorasChofer;                // 8 normal
        private Integer maxHorasExcepcionales;         // 10 máx
        private Integer descansoInterprovincialMin;    // 120 min
        private Integer descansoIntraprovincialMin;    // 45 min
        private Integer umbralInterprovincialKm;       // 100 km
        private String horaInicioOperacion;            // "05:00"
        private String horaFinOperacion;               // "22:00"
    }

    // ============ Preview/Response ============

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreviewGeneracionInteligente {
        private Integer totalFrecuencias;
        private Integer frecuenciasPorDia;
        private Integer diasOperacion;
        private Integer busesUtilizados;
        private Integer busesDisponibles;
        private List<FrecuenciaPreview> frecuencias;
        private Map<String, Integer> frecuenciasPorRuta;  // "Quito → Guayaquil": 15
        private Map<String, Integer> frecuenciasPorBus;   // "ABC-123": 8
        private List<String> advertencias;
        private List<String> errores;
        private Boolean esViable;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrecuenciaPreview {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate fecha;
        private String diaSemana;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime horaSalida;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime horaLlegada;
        private Long terminalOrigenId;
        private String terminalOrigenNombre;
        private Long terminalDestinoId;
        private String terminalDestinoNombre;
        private Long busId;
        private String busPlaca;
        private Integer asientosDisponibles;           // Asientos del bus
        private String tipoFrecuencia;                 // INTERPROVINCIAL/INTRAPROVINCIAL
        private Integer duracionMinutos;
        private Integer tiempoDescansoMinutos;
        private Integer paradasPermitidas;
        private Double precio;
        private Double distanciaKm;                    // Kilometros de la ruta
        private Double costoCombustibleEstimado;       // Costo estimado de combustible
        private Long choferId;                         // ID del chofer asignado
        private String choferNombre;                   // Nombre del chofer
        private Integer ordenDia;                      // Orden del viaje en el día
        private String esViajeDe;                      // "IDA" o "VUELTA"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultadoGeneracionInteligente {
        private Boolean exito;
        private Integer frecuenciasCreadas;
        private List<String> mensajes;
        private List<String> advertencias;
    }

    // ============ Paradas Intermedias ============

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParadaIntermedioConfig {
        private Long terminalId;
        private String terminalNombre;
        private Integer tiempoParadaMinutos;           // Tiempo de parada (5-15 min)
        private Integer ordenParada;                   // 1, 2, 3
        private Boolean recogerPasajeros;              // true por defecto
        private Boolean dejarPasajeros;                // true por defecto
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor  
    public static class TerminalIntermedio {
        private Long id;
        private String nombre;
        private Double distanciaDesdeOrigenKm;
        private Integer tiempoDesdeOrigenMinutos;
        private Boolean estaEnRuta;                    // true si está en el trayecto
    }
}
