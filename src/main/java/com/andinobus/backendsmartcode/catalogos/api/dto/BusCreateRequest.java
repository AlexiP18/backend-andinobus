package com.andinobus.backendsmartcode.catalogos.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BusCreateRequest {
    @Size(max = 50)
    private String numeroInterno;

    @NotBlank
    @Size(max = 20)
    private String placa;

    @Size(max = 100)
    private String chasisMarca;

    @Size(max = 100)
    private String carroceriaMarca;

    @Size(max = 500)
    private String fotoUrl;

    private Integer capacidadAsientos = 40;
    
    private Boolean tieneDosNiveles = false;
    
    private Integer capacidadPiso1;
    
    private Integer capacidadPiso2;

    private Boolean activo = true;
}
