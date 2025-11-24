package com.andinobus.backendsmartcode.ubicacion.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Canton {
    private String nombre;
    private Double latitud;
    private Double longitud;
    private Boolean esCapital;
}
