package com.andinobus.backendsmartcode.embarque.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class EmbarqueDtos {
    @Data
    public static class ScanRequest {
        private String codigo; // código de boleto o QR
    }

    @Data
    @Builder
    public static class ScanResponse {
        private String codigo;
        private boolean valido;
        private String estado; // emitido/usado/anulado
        private String message;
    }
}
