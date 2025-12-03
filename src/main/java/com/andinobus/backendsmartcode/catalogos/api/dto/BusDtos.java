package com.andinobus.backendsmartcode.catalogos.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class BusDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBusRequest {
        private Long cooperativaId;
        private String numeroInterno;
        private String placa;
        private String chasisMarca;
        private String carroceriaMarca;
        private String fotoUrl;
        private Integer capacidadAsientos;
        private Boolean tieneDosNiveles;
        private Integer capacidadPiso1;
        private Integer capacidadPiso2;
        private String estado; // DISPONIBLE | EN_SERVICIO | MANTENIMIENTO | PARADA
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateBusRequest {
        private String numeroInterno;
        private String placa;
        private String chasisMarca;
        private String carroceriaMarca;
        private String fotoUrl;
        private Integer capacidadAsientos;
        private Boolean tieneDosNiveles;
        private Integer capacidadPiso1;
        private Integer capacidadPiso2;
        private String estado;
        private Boolean activo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusResponse {
        private Long id;
        private Long cooperativaId;
        private String cooperativaNombre;
        private String numeroInterno;
        private String placa;
        private String chasisMarca;
        private String carroceriaMarca;
        private String fotoUrl;
        private Integer capacidadAsientos;
        private Boolean tieneDosNiveles;
        private Integer capacidadPiso1;
        private Integer capacidadPiso2;
        private String estado;
        private Boolean activo;
    }
}
