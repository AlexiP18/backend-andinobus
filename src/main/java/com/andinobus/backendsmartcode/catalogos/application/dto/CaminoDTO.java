package com.andinobus.backendsmartcode.catalogos.application.dto;

import com.andinobus.backendsmartcode.catalogos.domain.Camino;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaminoDTO {

    private Long id;
    private Long rutaId;
    private String rutaOrigen;
    private String rutaDestino;
    private String nombre;
    private Double distanciaKm;
    private Integer duracionMinutos;
    private String tipo;
    private String polyline;
    private Boolean activo;
    private List<ParadaCaminoDTO> paradas;

    public static CaminoDTO fromEntity(Camino camino) {
        return CaminoDTO.builder()
                .id(camino.getId())
                .rutaId(camino.getRuta().getId())
                .rutaOrigen(camino.getRuta().getOrigen())
                .rutaDestino(camino.getRuta().getDestino())
                .nombre(camino.getNombre())
                .distanciaKm(camino.getDistanciaKm())
                .duracionMinutos(camino.getDuracionMinutos())
                .tipo(camino.getTipo().name())
                .polyline(camino.getPolyline())
                .activo(camino.getActivo())
                .build();
    }

    public static CaminoDTO fromEntityWithParadas(Camino camino, List<ParadaCaminoDTO> paradas) {
        CaminoDTO dto = fromEntity(camino);
        dto.setParadas(paradas);
        return dto;
    }
}
