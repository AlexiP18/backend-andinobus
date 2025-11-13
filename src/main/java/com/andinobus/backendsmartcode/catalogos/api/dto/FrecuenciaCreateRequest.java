package com.andinobus.backendsmartcode.catalogos.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FrecuenciaCreateRequest {
    @NotBlank
    @Size(max = 120)
    private String origen;

    @NotBlank
    @Size(max = 120)
    private String destino;

    // Se acepta como texto flexible (HH:mm o HH:mm:ss). El servicio lo parsea.
    @NotBlank
    private String horaSalida;

    @Min(0)
    private Integer duracionEstimadaMin;

    @Size(max = 32)
    private String diasOperacion;

    private Boolean activa = true;
}
