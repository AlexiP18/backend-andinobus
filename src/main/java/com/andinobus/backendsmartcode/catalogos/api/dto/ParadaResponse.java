package com.andinobus.backendsmartcode.catalogos.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParadaResponse {
    private Long id;
    private Long frecuenciaId;
    private String ciudad;
    private Integer orden;
    private String horaEstimada;
}
