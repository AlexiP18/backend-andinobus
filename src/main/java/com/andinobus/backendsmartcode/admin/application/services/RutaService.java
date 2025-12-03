package com.andinobus.backendsmartcode.admin.application.services;

import com.andinobus.backendsmartcode.admin.api.dto.RutaDtos.*;
import com.andinobus.backendsmartcode.admin.domain.entities.Ruta;
import com.andinobus.backendsmartcode.admin.domain.repositories.RutaRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.CaminoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Profile("dev")
@Service
@RequiredArgsConstructor
public class RutaService {

    private final RutaRepository rutaRepository;
    private final CaminoRepository caminoRepository;

    @Transactional(readOnly = true)
    public List<RutaResponse> getAllRutas() {
        return rutaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RutaResponse> getRutasActivas() {
        return rutaRepository.findByActivoTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RutaResponse> getRutasAprobadas() {
        return rutaRepository.findByActivoTrueAndAprobadaAntTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RutaResponse> getRutasByTipo(String tipoRuta) {
        return rutaRepository.findByActivoTrueAndTipoRuta(tipoRuta).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RutaResponse getRutaById(Long id) {
        Ruta ruta = rutaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada con ID: " + id));
        return toResponse(ruta);
    }

    @Transactional
    public RutaResponse createRuta(CreateRutaRequest request) {
        if (rutaRepository.existsByNombreAndActivoTrue(request.getNombre())) {
            throw new RuntimeException("Ya existe una ruta activa con el nombre: " + request.getNombre());
        }

        Ruta ruta = Ruta.builder()
                .nombre(request.getNombre())
                .origen(request.getOrigen())
                .destino(request.getDestino())
                .distanciaKm(request.getDistanciaKm())
                .duracionEstimadaMinutos(request.getDuracionEstimadaMinutos())
                .descripcion(request.getDescripcion())
                .aprobadaAnt(request.getAprobadaAnt() != null ? request.getAprobadaAnt() : false)
                .numeroResolucionAnt(request.getNumeroResolucionAnt())
                .fechaAprobacionAnt(request.getFechaAprobacionAnt())
                .vigenciaHasta(request.getVigenciaHasta())
                .observacionesAnt(request.getObservacionesAnt())
                .activo(true)
                .build();

        Ruta savedRuta = rutaRepository.save(ruta);
        log.info("Ruta creada exitosamente: {}", savedRuta.getNombre());
        return toResponse(savedRuta);
    }

    @Transactional
    public RutaResponse updateRuta(Long id, UpdateRutaRequest request) {
        Ruta ruta = rutaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada con ID: " + id));

        if (request.getNombre() != null && !request.getNombre().equals(ruta.getNombre())) {
            if (rutaRepository.existsByNombreAndActivoTrue(request.getNombre())) {
                throw new RuntimeException("Ya existe una ruta activa con el nombre: " + request.getNombre());
            }
            ruta.setNombre(request.getNombre());
        }

        if (request.getOrigen() != null) ruta.setOrigen(request.getOrigen());
        if (request.getDestino() != null) ruta.setDestino(request.getDestino());
        if (request.getDistanciaKm() != null) ruta.setDistanciaKm(request.getDistanciaKm());
        if (request.getDuracionEstimadaMinutos() != null) ruta.setDuracionEstimadaMinutos(request.getDuracionEstimadaMinutos());
        if (request.getDescripcion() != null) ruta.setDescripcion(request.getDescripcion());
        if (request.getAprobadaAnt() != null) ruta.setAprobadaAnt(request.getAprobadaAnt());
        if (request.getNumeroResolucionAnt() != null) ruta.setNumeroResolucionAnt(request.getNumeroResolucionAnt());
        if (request.getFechaAprobacionAnt() != null) ruta.setFechaAprobacionAnt(request.getFechaAprobacionAnt());
        if (request.getVigenciaHasta() != null) ruta.setVigenciaHasta(request.getVigenciaHasta());
        if (request.getObservacionesAnt() != null) ruta.setObservacionesAnt(request.getObservacionesAnt());
        if (request.getActivo() != null) ruta.setActivo(request.getActivo());

        Ruta updatedRuta = rutaRepository.save(ruta);
        log.info("Ruta actualizada exitosamente: {}", updatedRuta.getNombre());
        return toResponse(updatedRuta);
    }

    @Transactional
    public void deleteRuta(Long id) {
        Ruta ruta = rutaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada con ID: " + id));
        ruta.setActivo(false);
        rutaRepository.save(ruta);
        log.info("Ruta desactivada exitosamente: {}", ruta.getNombre());
    }

    private RutaResponse toResponse(Ruta ruta) {
        // Contar caminos activos asociados a la ruta
        int cantidadCaminos = caminoRepository.findByRutaIdAndActivoTrue(ruta.getId()).size();
        
        return RutaResponse.builder()
                .id(ruta.getId())
                .nombre(ruta.getNombre())
                .origen(ruta.getOrigen())
                .destino(ruta.getDestino())
                .distanciaKm(ruta.getDistanciaKm())
                .duracionEstimadaMinutos(ruta.getDuracionEstimadaMinutos())
                .descripcion(ruta.getDescripcion())
                .aprobadaAnt(ruta.getAprobadaAnt())
                .numeroResolucionAnt(ruta.getNumeroResolucionAnt())
                .fechaAprobacionAnt(ruta.getFechaAprobacionAnt())
                .vigenciaHasta(ruta.getVigenciaHasta())
                .observacionesAnt(ruta.getObservacionesAnt())
                .activo(ruta.getActivo())
                .tipoRuta(ruta.getTipoRuta())
                .terminalOrigenId(ruta.getTerminalOrigen() != null ? ruta.getTerminalOrigen().getId() : null)
                .terminalDestinoId(ruta.getTerminalDestino() != null ? ruta.getTerminalDestino().getId() : null)
                .terminalOrigenNombre(ruta.getTerminalOrigen() != null ? ruta.getTerminalOrigen().getNombre() : null)
                .terminalDestinoNombre(ruta.getTerminalDestino() != null ? ruta.getTerminalDestino().getNombre() : null)
                .cantidadCaminos(cantidadCaminos)
                .build();
    }
}
