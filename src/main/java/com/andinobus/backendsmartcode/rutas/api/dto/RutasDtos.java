package com.andinobus.backendsmartcode.rutas.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

public class RutasDtos {

    @Data
    @Builder
    public static class SearchRouteItem {
        private Long frecuenciaId;
        private Long cooperativaId;
        private String cooperativa;
        private String origen;
        private String destino;
        private String horaSalida;
        private String duracionEstimada;
        private String tipoViaje; // directo | con_paradas
        private Map<String, Integer> asientosPorTipo; // Normal/VIP → disponibles (mock)
    }

    @Data
    @Builder
    public static class SearchRouteResponse {
        private List<SearchRouteItem> items;
        private Integer total;
        private Integer page;
        private Integer size;
    }

    @Data
    @Builder
    public static class DisponibilidadResponse {
        private Long viajeId;
        private Integer totalAsientos;
        private Integer disponibles;
        private Map<String, Integer> porTipo;
    }

    @Data
    @Builder
    public static class BusFichaResponse {
        private Long viajeId;
        private Long busId;
        private String cooperativa;
        private String numeroInterno;
        private String placa;
        private String chasisMarca;
        private String carroceriaMarca;
        private String fotoUrl;
    }

    @Data
    @Builder
    public static class ViajeItem {
        private Long id;
        private Long frecuenciaId;
        private String fecha; // YYYY-MM-DD
        private String origen;
        private String destino;
        private String horaSalida;
        private String estado; // programado | finalizado | cancelado (mock)
    }

    @Data
    @Builder
    public static class ViajesResponse {
        private List<ViajeItem> items;
        private Integer total;
        private Integer page;
        private Integer size;
    }
}
