package com.andinobus.backendsmartcode.cooperativa.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTOs para el módulo de reportes de cooperativa
 */
public class ReporteCooperativaDtos {

    /**
     * Resumen general de la cooperativa
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenCooperativaResponse {
        // Ventas
        private BigDecimal ventasTotales;
        private BigDecimal ventasCambio; // % cambio respecto al periodo anterior
        private Integer totalTransacciones;
        private BigDecimal ticketPromedio;
        
        // Viajes
        private Integer totalViajes;
        private Integer viajesCompletados;
        private Integer viajesCancelados;
        private Integer viajesPendientes;
        
        // Ocupación
        private Double ocupacionPromedio;
        private Double ocupacionMasAlta;
        private Double ocupacionMasBaja;
        private Integer asientosTotalesVendidos;
        
        // Recursos
        private Integer totalBuses;
        private Integer busesActivos;
        private Integer totalChoferes;
        private Integer choferesActivos;
        private Integer totalRutas;
        private Integer rutasActivas;
    }
    
    /**
     * Reporte de ventas detallado
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReporteVentasResponse {
        private BigDecimal ventasTotales;
        private BigDecimal cambioVentas; // % cambio
        private Integer totalTransacciones;
        private BigDecimal ticketPromedio;
        private BigDecimal ventasDiarias; // Promedio por día
        
        // Ventas por día
        private List<VentaDiariaDto> ventasPorDia;
        
        // Top rutas por ventas
        private List<RutaVentasDto> topRutas;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VentaDiariaDto {
        private LocalDate fecha;
        private String diaSemana;
        private BigDecimal monto;
        private Integer transacciones;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RutaVentasDto {
        private Long rutaId;
        private String nombreRuta; // origen -> destino
        private String terminalOrigen;
        private String terminalDestino;
        private BigDecimal ventas;
        private Integer boletos;
    }
    
    /**
     * Reporte de viajes detallado
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReporteViajesResponse {
        private Integer totalViajes;
        private Integer viajesCompletados;
        private Integer viajesCancelados;
        private Integer viajesPendientes;
        private Integer viajesEnRuta;
        private Double porcentajeCompletados;
        private Double porcentajeCancelados;
        
        // Viajes por estado
        private List<ViajeEstadoDto> viajesPorEstado;
        
        // Viajes por día
        private List<ViajeDiarioDto> viajesPorDia;
        
        // Viajes por ruta
        private List<ViajeRutaDto> viajesPorRuta;
        
        // Viajes por bus
        private List<ViajeBusDto> viajesPorBus;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViajeEstadoDto {
        private String estado;
        private Integer cantidad;
        private Double porcentaje;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViajeDiarioDto {
        private LocalDate fecha;
        private String diaSemana;
        private Integer total;
        private Integer completados;
        private Integer cancelados;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViajeRutaDto {
        private Long rutaId;
        private String nombreRuta;
        private Integer totalViajes;
        private Integer viajesCompletados;
        private Double porcentajeOcupacion;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViajeBusDto {
        private Long busId;
        private String placa;
        private Integer totalViajes;
        private Integer viajesCompletados;
        private Double horasTrabajadas;
    }
    
    /**
     * Reporte de ocupación detallado
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReporteOcupacionResponse {
        private Double ocupacionPromedio;
        private Double ocupacionMasAlta;
        private Double ocupacionMasBaja;
        private Integer asientosTotales;
        private Integer asientosVendidos;
        private Integer asientosDisponibles;
        
        // Ocupación por día
        private List<OcupacionDiariaDto> ocupacionPorDia;
        
        // Ocupación por ruta
        private List<OcupacionRutaDto> ocupacionPorRuta;
        
        // Ocupación por hora
        private List<OcupacionHoraDto> ocupacionPorHora;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OcupacionDiariaDto {
        private LocalDate fecha;
        private String diaSemana;
        private Double porcentaje;
        private Integer asientosVendidos;
        private Integer asientosTotales;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OcupacionRutaDto {
        private String nombreRuta;
        private Double ocupacionPromedio;
        private Integer viajes;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OcupacionHoraDto {
        private Integer hora;
        private Double ocupacionPromedio;
        private Integer viajes;
    }
    
    /**
     * Reporte de rutas detallado
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReporteRutasResponse {
        private Integer totalRutas;
        private Integer rutasActivas;
        private Integer frecuenciasActivas;
        
        // Detalle por ruta
        private List<DetalleRutaDto> rutas;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleRutaDto {
        private Long rutaId;
        private String terminalOrigen;
        private String terminalDestino;
        private String nombreRuta;
        private Double distanciaKm;
        private Integer duracionMinutos;
        private BigDecimal precioBase;
        private Integer frecuenciasActivas;
        private Integer viajesRealizados;
        private BigDecimal ingresosTotales;
        private Double ocupacionPromedio;
        private boolean activa;
    }
    
    /**
     * Request para solicitar reportes
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReporteRequest {
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private String tipoReporte; // ventas, viajes, ocupacion, rutas
        private Long rutaId; // Opcional: filtrar por ruta
        private Long busId;  // Opcional: filtrar por bus
    }
}
