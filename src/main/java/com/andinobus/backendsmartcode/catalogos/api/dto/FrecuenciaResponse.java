package com.andinobus.backendsmartcode.catalogos.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FrecuenciaResponse {
    private Long id;
    private Long cooperativaId;
    private String origen;
    private String destino;
    private String horaSalida; // HH:mm
    private Integer duracionEstimadaMin;
    private String diasOperacion;
    private Boolean activa;
}
