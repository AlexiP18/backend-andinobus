package com.andinobus.backendsmartcode.catalogos.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CooperativaResponse {
    private Long id;
    private String nombre;
    private String ruc;
    private String logoUrl;
    private Boolean activo;
}
