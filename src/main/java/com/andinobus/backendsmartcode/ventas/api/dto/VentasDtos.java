package com.andinobus.backendsmartcode.ventas.api.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class VentasDtos {

    @Data
    public static class ReservaCreateRequest {
        private Long viajeId;
        private String tramoOrigen; // opcional para subtramos
        private String tramoDestino;
        private List<String> asientos; // ej.: ["1A","1B"]
        private String tipoAsiento; // Normal/VIP
        private Long clienteId; // opcional en stub
        private String clienteEmail; // email del cliente (dev)
    }

    @Data
    @Builder
    public static class ReservaResponse {
        private Long id;
        private Long viajeId;
        private List<String> asientos;
        private String estado; // pendiente/pagado/caducado/cancelado
        private String fechaExpira; // ISO8601
    }

    @Data
    @Builder
    public static class ReservaDetalleResponse {
        private Long id;
        private Long viajeId;
        private String cliente;
        private List<String> asientos;
        private String estado;
        private BigDecimal monto;
        
        // Información del viaje
        private String fecha;
        private String horaSalida;
        private String origen;
        private String destino;
        private String busPlaca;
        private String cooperativaNombre;
        private String rutaNombre;
        private String codigoBoleto;
    }

    @Data
    public static class PagoTransferenciaRequest {
        private Long reservaId;
        private BigDecimal monto;
        private String referencia;
        // Multipart "comprobante" en el request
    }

    @Data
    public static class PaypalWebhookEvent {
        private String event_type;
        private String resource_id;
        private BigDecimal amount;
        private String currency;
    }

    @Data
    public static class EmitirBoletoRequest {
        private Long reservaId;
    }

    @Data
    @Builder
    public static class BoletoResponse {
        private String codigoBoleto;
        private Long reservaId;
        private String estado;
        private String codigoQR; // data URL base64
    }

    @Data
    @Builder
    public static class AsientoDisponibilidadDto {
        private String numeroAsiento; // "1A", "2B", etc.
        private String tipoAsiento; // "NORMAL", "VIP", "ACONDICIONADO"
        private String estado; // "DISPONIBLE", "RESERVADO", "VENDIDO", "BLOQUEADO"
        private Integer fila; // Fila del asiento en el layout
        private Integer columna; // Columna del asiento en el layout (0-4)
    }

    @Data
    @Builder
    public static class PagoConfirmacionRequest {
        private Long reservaId;
        private String metodoPago; // "EFECTIVO", "TARJETA", "PAYPAL"
        private String referencia; // Opcional: número de transacción
    }

    @Data
    @Builder
    public static class PagoResponse {
        private Long reservaId;
        private String estado; // "PAGADO", "RECHAZADO"
        private String mensaje;
    }

    @Data
    @Builder
    public static class ReservaCooperativaDto {
        private Long id;
        private Long viajeId;
        private String clienteEmail;
        private Integer asientos;
        private String estado;
        private BigDecimal monto;
        private String expiresAt;
        private String createdAt;
        // Datos del viaje
        private String fecha;
        private String horaSalida;
        private String origen;
        private String destino;
        private String busPlaca;
        private String rutaNombre;
    }
}
