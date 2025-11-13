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
        private String codigo;
        private Long reservaId;
        private String estado;
        private String qr; // data URL o placeholder
    }
}
