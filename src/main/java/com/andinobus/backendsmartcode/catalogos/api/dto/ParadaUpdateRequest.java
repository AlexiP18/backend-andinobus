package com.andinobus.backendsmartcode.catalogos.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ParadaUpdateRequest {
    @Size(max = 120)
    private String ciudad;

    @Min(0)
    private Integer orden;

    // Hora estimada de llegada/salida (opcional). Formatos flexibles.
    private String horaEstimada;
}
