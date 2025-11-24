package com.andinobus.backendsmartcode.ventas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class VentaPresencialDtos {

    /**
     * DTO para crear una venta presencial desde una frecuencia
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateVentaPresencialRequest {
        private Long cooperativaId;
        private Long frecuenciaId;
        private String fecha; // YYYY-MM-DD
        private List<String> asientos; // Nombres de asientos ["A1", "B2"]
        private String clienteNombres;
        private String clienteApellidos;
        private String clienteCedula;
        private String clienteTelefono;
        private String clienteEmail;
        private String metodoPago; // EFECTIVO | TARJETA
        private BigDecimal precioTotal;
    }

    /**
     * DTO de respuesta después de crear la venta
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VentaPresencialResponse {
        private Long reservaId;
        private Long viajeId;
        private List<String> asientos; // números de asiento
        private String clienteNombres;
        private String clienteApellidos;
        private String clienteCedula;
        private BigDecimal totalPagado;
        private String metodoPago;
        private String estado; // PAGADO
        private String mensaje;
    }
}
