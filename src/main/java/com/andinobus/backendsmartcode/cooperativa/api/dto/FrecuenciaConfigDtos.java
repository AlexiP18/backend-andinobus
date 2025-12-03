package com.andinobus.backendsmartcode.cooperativa.api.dto;

import lombok.*;

import java.time.LocalTime;
import java.util.List;

public class FrecuenciaConfigDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrecuenciaConfigResponse {
        private Long id;
        private Long cooperativaId;
        private String cooperativaNombre;
        
        // Configuración de precios
        private Double precioBasePorKm;
        private Double factorDieselPorKm;
        private Double precioDiesel;
        private Double margenGananciaPorcentaje;
        
        // Configuración de choferes
        private Integer maxHorasDiariasChofer;
        private Integer maxHorasExcepcionales;
        private Integer maxDiasExcepcionalesSemana;
        private Integer tiempoDescansoEntreViajesMinutos;
        
        // Configuración de buses
        private Integer tiempoMinimoParadaBusMinutos;
        private Integer horasOperacionMaxBus;
        
        // Configuración de generación automática
        private Integer intervaloMinimoFrecuenciasMinutos;
        private String horaInicioOperacion;
        private String horaFinOperacion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateFrecuenciaConfigRequest {
        private Double precioBasePorKm;
        private Double factorDieselPorKm;
        private Double precioDiesel;
        private Double margenGananciaPorcentaje;
        private Integer maxHorasDiariasChofer;
        private Integer maxHorasExcepcionales;
        private Integer maxDiasExcepcionalesSemana;
        private Integer tiempoDescansoEntreViajesMinutos;
        private Integer tiempoMinimoParadaBusMinutos;
        private Integer horasOperacionMaxBus;
        private Integer intervaloMinimoFrecuenciasMinutos;
        private String horaInicioOperacion;
        private String horaFinOperacion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RutaDisponibleResponse {
        private Long rutaId;
        private String rutaNombre;
        private Long terminalOrigenId;
        private String terminalOrigenNombre;
        private String terminalOrigenCanton;
        private String terminalOrigenProvincia;
        private Long terminalDestinoId;
        private String terminalDestinoNombre;
        private String terminalDestinoCanton;
        private String terminalDestinoProvincia;
        private Double distanciaKm;
        private Integer duracionEstimadaMinutos;
        private Double precioSugerido;
        private List<TerminalIntermedio> terminalesIntermedios;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TerminalIntermedio {
        private Long terminalId;
        private String terminalNombre;
        private String canton;
        private String provincia;
        private Integer ordenEnRuta;
        private Double distanciaDesdeOrigen;
        private Integer tiempoDesdeOrigen;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusDisponibilidadResponse {
        private Long busId;
        private String placa;
        private String numeroInterno;
        private Integer capacidadAsientos;
        private String estado;
        private Double horasOperadasHoy;
        private Double horasDisponiblesHoy;
        private Integer frecuenciasHoy;
        private Boolean disponible;
        private String motivoNoDisponible;
        private List<ChoferAsignado> choferesAsignados;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChoferAsignado {
        private Long choferId;
        private String nombre;
        private String tipo; // PRINCIPAL, ALTERNO
        private Double horasTrabajadasHoy;
        private Double horasDisponiblesHoy;
        private Boolean disponible;
        private Integer diasExcepcionalesSemana;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChoferDisponibilidadResponse {
        private Long choferId;
        private String nombre;
        private String cedula;
        private String telefono;
        private Double horasTrabajadasHoy;
        private Double horasDisponiblesHoy;
        private Boolean puedeTrabajarHorasExcepcionales;
        private Integer diasExcepcionalesUsadosSemana;
        private Integer frecuenciasHoy;
        private Boolean disponible;
        private String motivoNoDisponible;
        private Long busAsignadoId;
        private String busAsignadoPlaca;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CrearFrecuenciaValidadaRequest {
        private Long busId;
        private Long choferId;
        private Long terminalOrigenId;
        private Long terminalDestinoId;
        private String horaSalida; // "HH:mm"
        private String diasOperacion; // "LUNES,MARTES..."
        private Double precioBase;
        private String observaciones;
        private List<ParadaFrecuenciaRequest> paradas;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParadaFrecuenciaRequest {
        private Integer orden;
        private Long terminalId;
        private String tiempoLlegada; // "HH:mm"
        private Integer tiempoEsperaMinutos;
        private Double precioDesdeOrigen;
        private Boolean permiteAbordaje;
        private Boolean permiteDescenso;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerarFrecuenciasAutomaticasRequest {
        private Long terminalOrigenId;
        private Long terminalDestinoId;
        private String fechaInicio; // "yyyy-MM-dd"
        private String fechaFin; // "yyyy-MM-dd"
        private String horaInicioOperacion; // "HH:mm"
        private String horaFinOperacion; // "HH:mm"
        private Integer intervaloMinutos;
        private String diasOperacion; // "LUNES,MARTES..."
        private Boolean incluirParadasIntermedias;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrecuenciaGeneradaResponse {
        private Long id;
        private Long busId;
        private String busPlaca;
        private Long choferId;
        private String choferNombre;
        private Long terminalOrigenId;
        private String terminalOrigenNombre;
        private Long terminalDestinoId;
        private String terminalDestinoNombre;
        private String horaSalida;
        private String horaLlegadaEstimada;
        private Double precioBase;
        private String diasOperacion;
        private Integer duracionMinutos;
        private Double distanciaKm;
        private List<ParadaResponse> paradas;
        private Boolean activa;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParadaResponse {
        private Long id;
        private Integer orden;
        private Long terminalId;
        private String terminalNombre;
        private String tiempoLlegada;
        private Integer tiempoEsperaMinutos;
        private Double precioDesdeOrigen;
        private Boolean permiteAbordaje;
        private Boolean permiteDescenso;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidacionFrecuenciaResponse {
        private Boolean valida;
        private List<String> errores;
        private List<String> advertencias;
        private BusDisponibilidadResponse busDisponibilidad;
        private ChoferDisponibilidadResponse choferDisponibilidad;
        private Double precioSugerido;
        private String horaLlegadaEstimada;
        private Integer duracionEstimadaMinutos;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenGeneracionResponse {
        private Integer frecuenciasGeneradas;
        private Integer frecuenciasOmitidas;
        private List<String> mensajes;
        private List<FrecuenciaGeneradaResponse> frecuencias;
    }
}
