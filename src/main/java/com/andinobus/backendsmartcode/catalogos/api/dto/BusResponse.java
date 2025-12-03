package com.andinobus.backendsmartcode.catalogos.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusResponse {
    private Long id;
    private Long cooperativaId;
    private String numeroInterno;
    private String placa;
    private String chasisMarca;
    private String carroceriaMarca;
    private String fotoUrl;
    private Integer capacidadAsientos;
    private Boolean tieneDosNiveles;
    private Integer capacidadPiso1;
    private Integer capacidadPiso2;
    private Boolean activo;
}
