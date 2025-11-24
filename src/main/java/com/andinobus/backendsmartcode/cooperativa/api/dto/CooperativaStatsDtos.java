package com.andinobus.backendsmartcode.cooperativa.api.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class CooperativaStatsDtos {

    @Data
    @Builder
    public static class CooperativaStats {
        private Long cooperativaId;
        private String nombre;
        private Integer totalBuses;
        private Integer busesActivos;
        private Integer totalPersonal;
        private BigDecimal ventasDelMes;
        private BigDecimal ventasDeHoy;
    }

    @Data
    @Builder
    public static class AdminStats {
        private Integer busesActivos;
        private Integer totalPersonal;
        private BigDecimal ventasHoy;
        private Integer viajesHoy;
        private Integer choferes;
        private Integer oficinistas;
    }

    @Data
    @Builder
    public static class OficinistaStats {
        private Integer boletosVendidosHoy;
        private BigDecimal recaudadoHoy;
        private Integer reservasPendientes;
        private Integer viajesProgramados;
        private Integer pasajerosRegistrados;
    }

    @Data
    @Builder
    public static class ChoferStats {
        private Integer viajesDelMes;
        private Integer pasajerosTransportados;
        private BigDecimal calificacion;
        private ViajeActual viajeActual;
    }

    @Data
    @Builder
    public static class ViajeActual {
        private Long viajeId;
        private String origen;
        private String destino;
        private LocalDate fecha;
        private LocalTime horaSalida;
        private String busPlaca;
        private Integer pasajerosConfirmados;
        private String estado;
    }

    @Data
    @Builder
    public static class BusesList {
        private List<BusInfo> buses;
        private Integer total;
    }

    @Data
    @Builder
    public static class BusInfo {
        private Long id;
        private String placa;
        private String modelo;
        private Integer capacidad;
        private String estado;
        private Integer anioFabricacion;
    }

    @Data
    @Builder
    public static class PersonalList {
        private List<PersonalInfo> personal;
        private Integer total;
    }

    @Data
    @Builder
    public static class PersonalInfo {
        private Long id;
        private String nombres;
        private String apellidos;
        private String email;
        private String rolCooperativa;
        private String cedula;
        private String telefono;
        private Boolean activo;
    }
}
