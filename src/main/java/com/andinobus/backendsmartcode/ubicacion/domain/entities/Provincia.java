package com.andinobus.backendsmartcode.ubicacion.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Provincia {
    private String nombre;
    private String capital;
    private Double latitud;
    private Double longitud;
    private List<Canton> cantones;
}
