package com.andinobus.backendsmartcode.catalogos.application.dto;

import com.andinobus.backendsmartcode.catalogos.domain.Parada;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParadaCaminoDTO {

    private Long id;
    private Long caminoId;
    private String nombre;
    private String direccion;
    private Double latitud;
    private Double longitud;
    private Integer orden;
    private Integer tiempoEstimadoMinutos;
    private Boolean permiteAbordaje;
    private Boolean permiteDescenso;
    private BigDecimal precioDesdeOrigen;
    private Boolean activa;

    public static ParadaCaminoDTO fromEntity(Parada parada) {
        return ParadaCaminoDTO.builder()
                .id(parada.getId())
                .caminoId(parada.getCamino().getId())
                .nombre(parada.getNombre())
                .direccion(parada.getDireccion())
                .latitud(parada.getLatitud())
                .longitud(parada.getLongitud())
                .orden(parada.getOrden())
                .tiempoEstimadoMinutos(parada.getTiempoEstimadoMinutos())
                .permiteAbordaje(parada.getPermiteAbordaje())
                .permiteDescenso(parada.getPermiteDescenso())
                .precioDesdeOrigen(parada.getPrecioDesdeOrigen())
                .activa(parada.getActiva())
                .build();
    }
}
