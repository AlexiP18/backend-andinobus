package com.andinobus.backendsmartcode.catalogos.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class AsientoLayoutDtos {

    /**
     * DTO para representar un asiento individual
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AsientoResponse {
        private Long id;
        private Integer numeroAsiento;
        private Integer fila;
        private Integer columna;
        private String tipoAsiento; // NORMAL | VIP | ACONDICIONADO
        private Boolean habilitado;
    }

    /**
     * DTO para la configuración completa del layout de asientos de un bus
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BusLayoutResponse {
        private Long busId;
        private String placa;
        private Integer capacidadTotal;
        private Integer capacidadHabilitada;
        private Integer maxFilas;
        private Integer maxColumnas;
        private List<AsientoResponse> asientos;
    }

    /**
     * DTO para crear un asiento
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateAsientoRequest {
        private Integer numeroAsiento;
        private Integer fila;
        private Integer columna;
        private String tipoAsiento; // NORMAL | VIP | ACONDICIONADO
        private Boolean habilitado;
    }

    /**
     * DTO para actualizar un asiento
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateAsientoRequest {
        private String tipoAsiento;
        private Boolean habilitado;
    }

    /**
     * DTO para generar el layout automático
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateLayoutRequest {
        private Integer filas;
        private Integer columnas;
        private Boolean sobrescribir; // Si true, elimina el layout existente
        private Boolean incluirFilaTrasera; // Si true, agrega una fila continua de 5 asientos al final
    }

    /**
     * DTO para actualizar múltiples asientos a la vez
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkUpdateAsientosRequest {
        private List<UpdateAsientoItem> asientos;
    }

    /**
     * Item individual para actualización en bulk
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateAsientoItem {
        private Long id;
        private Integer numeroAsiento;
        private Integer fila;
        private Integer columna;
        private String tipoAsiento;
        private Boolean habilitado;
    }

    /**
     * DTO de respuesta genérica
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AsientoOperationResponse {
        private Boolean success;
        private String message;
        private Integer asientosCreados;
        private Integer asientosActualizados;
    }
}
