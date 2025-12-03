package com.andinobus.backendsmartcode.operacion.application.dto;

import com.andinobus.backendsmartcode.operacion.domain.PosicionViaje;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosicionViajeDTO {

    private Long id;
    private Long viajeId;
    private Double latitud;
    private Double longitud;
    private Double velocidadKmh;
    private Double precision;
    private LocalDateTime timestamp;
    private String provider;

    public static PosicionViajeDTO fromEntity(PosicionViaje posicion) {
        return PosicionViajeDTO.builder()
                .id(posicion.getId())
                .viajeId(posicion.getViaje().getId())
                .latitud(posicion.getLatitud())
                .longitud(posicion.getLongitud())
                .velocidadKmh(posicion.getVelocidadKmh())
                .precision(posicion.getPrecision())
                .timestamp(posicion.getTimestamp())
                .provider(posicion.getProvider())
                .build();
    }
}
