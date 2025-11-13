package com.andinobus.backendsmartcode.catalogos.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ParadaCreateRequest {
    @NotBlank
    @Size(max = 120)
    private String ciudad;

    @NotNull
    @Min(0)
    private Integer orden;

    // Hora estimada de llegada/salida a la parada (opcional). Acepta formatos flexibles HH:mm, HH:mm:ss, 730, 7, 14h30, 7.30
    private String horaEstimada;
}
