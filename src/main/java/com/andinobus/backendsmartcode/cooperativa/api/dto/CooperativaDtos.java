package com.andinobus.backendsmartcode.cooperativa.api.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class CooperativaDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusDto {
        private Long id;
        private String numeroInterno;
        private String placa;
        private String chasisMarca;
        private String carroceriaMarca;
        private Integer capacidadAsientos;
        private String estado; // DISPONIBLE | EN_SERVICIO | MANTENIMIENTO | PARADA
        private Boolean activo;
        private String fotoUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrecuenciaDto {
        private Long id;
        private String origen;
        private String destino;
        private LocalTime horaSalida;
        private Integer duracionEstimadaMin;
        private String diasOperacion;
        private Boolean activa;
        private List<ParadaIntermediaDto> paradas;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParadaIntermediaDto {
        private Long id;
        private String ciudad;
        private Integer ordenParada;
        private Integer minutosDesdeOrigen;
        private Double precioAdicional;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AsignacionBusFrecuenciaDto {
        private Long id;
        private BusDto bus;
        private FrecuenciaDto frecuencia;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private String estado; // ACTIVA | SUSPENDIDA | FINALIZADA
        private String observaciones;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiaParadaBusDto {
        private Long id;
        private BusDto bus;
        private LocalDate fecha;
        private String motivo; // MANTENIMIENTO | EXCESO_CAPACIDAD | OTRO
        private String observaciones;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AsignarBusRequest {
        private Long busId;
        private Long frecuenciaId;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private String observaciones;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrarDiaParadaRequest {
        private Long busId;
        private LocalDate fecha;
        private String motivo;
        private String observaciones;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenDisponibilidadDto {
        private int totalBuses;
        private int busesDisponibles;
        private int busesEnServicio;
        private int busesMantenimiento;
        private int busesParada;
        private int frecuenciasActivas;
        private int excesoBuses;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CrearFrecuenciaRequest {
        private String origen;
        private String destino;
        private LocalTime horaSalida;
        private Integer duracionEstimadaMin;
        private String diasOperacion;
        private List<ParadaIntermediaRequest> paradas;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParadaIntermediaRequest {
        private String ciudad;
        private Integer ordenParada;
        private Integer minutosDesdeOrigen;
        private Double precioAdicional;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActualizarEstadoBusRequest {
        private String estado; // DISPONIBLE | EN_SERVICIO | MANTENIMIENTO | PARADA
        private String observaciones;
    }
}
