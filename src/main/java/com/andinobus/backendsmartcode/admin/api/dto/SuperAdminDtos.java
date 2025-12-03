package com.andinobus.backendsmartcode.admin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SuperAdminDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuperAdminStatsResponse {
        private int totalCooperativas;
        private int cooperativasActivas;
        private int totalBuses;
        private int busesActivos;
        private int totalUsuarios;
        private int usuariosActivos;
        private double ventasTotalesHoy;
        private int viajesHoy;
        private int reservasPendientes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CooperativaInfo {
        private Long id;
        private String nombre;
        private String ruc;
        private int cantidadBuses;
        private int cantidadPersonal;
        private boolean activo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CooperativaDetalleResponse {
        private Long id;
        private String nombre;
        private String ruc;
        private String direccion;
        private String telefono;
        private String email;
        private boolean activo;
        private CooperativaStatsResponse estadisticas;
        private java.util.List<BusInfo> buses;
        private java.util.List<UsuarioInfo> usuarios;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CooperativaStatsResponse {
        private int totalBuses;
        private int busesActivos;
        private int totalUsuarios;
        private int usuariosActivos;
        private int viajesHoy;
        private double ventasHoy;
        private int reservasPendientes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusInfo {
        private Long id;
        private String placa;
        private String modelo;
        private int capacidad;
        private boolean activo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioInfo {
        private Long id;
        private String nombres;
        private String apellidos;
        private String email;
        private String rol;
        private boolean activo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClienteInfo {
        private Long id;
        private String email;
        private String nombres;
        private String apellidos;
        private boolean activo;
        private String createdAt;
    }
}
