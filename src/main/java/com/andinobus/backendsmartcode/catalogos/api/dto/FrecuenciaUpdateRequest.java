package com.andinobus.backendsmartcode.catalogos.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FrecuenciaUpdateRequest {
    @Size(max = 120)
    private String origen;

    @Size(max = 120)
    private String destino;

    // Texto flexible (HH:mm o HH:mm:ss). El servicio lo parsea.
    private String horaSalida;

    @Min(0)
    private Integer duracionEstimadaMin;

    @Size(max = 32)
    private String diasOperacion;

    private Boolean activa;
}
