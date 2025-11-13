package com.andinobus.backendsmartcode.catalogos.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CooperativaCreateRequest {
    @NotBlank
    @Size(max = 200)
    private String nombre;

    @Size(max = 13)
    private String ruc;

    @Size(max = 500)
    private String logoUrl;

    private Boolean activo = true;
}
