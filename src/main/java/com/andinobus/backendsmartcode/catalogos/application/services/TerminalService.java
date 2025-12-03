package com.andinobus.backendsmartcode.catalogos.application.services;

import com.andinobus.backendsmartcode.catalogos.api.dto.TerminalDtos.*;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Terminal;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.TerminalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TerminalService {

    private final TerminalRepository terminalRepository;

    @Transactional(readOnly = true)
    public List<TerminalResponse> listarTodos() {
        return terminalRepository.findAllActivosOrdenados()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TerminalResponse obtenerPorId(Long id) {
        Terminal terminal = terminalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Terminal no encontrado con ID: " + id));
        return toResponse(terminal);
    }

    @Transactional(readOnly = true)
    public List<TerminalResponse> listarPorProvincia(String provincia) {
        return terminalRepository.findByProvinciaActivos(provincia)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TerminalResponse> listarPorTipologia(String tipologia) {
        return terminalRepository.findByTipologiaActivos(tipologia)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TerminalResponse> buscar(String texto) {
        return terminalRepository.buscarPorTexto(texto)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> listarProvincias() {
        return terminalRepository.findProvinciasConTerminales();
    }

    @Transactional
    public TerminalResponse crear(TerminalCreateRequest request) {
        if (terminalRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new RuntimeException("Ya existe un terminal con el nombre: " + request.getNombre());
        }

        Terminal terminal = Terminal.builder()
                .nombre(request.getNombre())
                .provincia(request.getProvincia())
                .canton(request.getCanton())
                .tipologia(request.getTipologia())
                .andenes(request.getAndenes() != null ? request.getAndenes() : 1)
                .frecuenciasPorAnden(request.getFrecuenciasPorAnden() != null ? request.getFrecuenciasPorAnden() : 96)
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .horarioApertura(request.getHorarioApertura())
                .horarioCierre(request.getHorarioCierre())
                .imagenUrl(request.getImagenUrl())
                .activo(true)
                .build();

        terminal = terminalRepository.save(terminal);
        return toResponse(terminal);
    }

    @Transactional
    public TerminalResponse actualizar(Long id, TerminalUpdateRequest request) {
        Terminal terminal = terminalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Terminal no encontrado con ID: " + id));

        if (request.getNombre() != null) terminal.setNombre(request.getNombre());
        if (request.getProvincia() != null) terminal.setProvincia(request.getProvincia());
        if (request.getCanton() != null) terminal.setCanton(request.getCanton());
        if (request.getTipologia() != null) terminal.setTipologia(request.getTipologia());
        if (request.getAndenes() != null) terminal.setAndenes(request.getAndenes());
        if (request.getFrecuenciasPorAnden() != null) terminal.setFrecuenciasPorAnden(request.getFrecuenciasPorAnden());
        if (request.getLatitud() != null) terminal.setLatitud(request.getLatitud());
        if (request.getLongitud() != null) terminal.setLongitud(request.getLongitud());
        if (request.getDireccion() != null) terminal.setDireccion(request.getDireccion());
        if (request.getTelefono() != null) terminal.setTelefono(request.getTelefono());
        if (request.getHorarioApertura() != null) terminal.setHorarioApertura(request.getHorarioApertura());
        if (request.getHorarioCierre() != null) terminal.setHorarioCierre(request.getHorarioCierre());
        // imagenUrl puede ser string vacío para eliminar la imagen, o un base64/URL para establecerla
        if (request.getImagenUrl() != null) {
            terminal.setImagenUrl(request.getImagenUrl().isEmpty() ? null : request.getImagenUrl());
        }
        if (request.getActivo() != null) terminal.setActivo(request.getActivo());

        terminal = terminalRepository.save(terminal);
        return toResponse(terminal);
    }

    @Transactional
    public void desactivar(Long id) {
        Terminal terminal = terminalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Terminal no encontrado con ID: " + id));
        terminal.setActivo(false);
        terminalRepository.save(terminal);
    }

    @Transactional(readOnly = true)
    public TerminalStatsResponse obtenerEstadisticas() {
        long totalTerminales = terminalRepository.count();
        long t1 = terminalRepository.countByTipologia("T1");
        long t2 = terminalRepository.countByTipologia("T2");
        long t3 = terminalRepository.countByTipologia("T3");
        long t4 = terminalRepository.countByTipologia("T4");
        long t5 = terminalRepository.countByTipologia("T5");
        Long maxFrecuencias = terminalRepository.sumMaxFrecuenciasTotales();

        return TerminalStatsResponse.builder()
                .totalTerminales(totalTerminales)
                .terminalesT1(t1)
                .terminalesT2(t2)
                .terminalesT3(t3)
                .terminalesT4(t4)
                .terminalesT5(t5)
                .capacidadTotalFrecuencias(maxFrecuencias != null ? maxFrecuencias : 0)
                .build();
    }

    private TerminalResponse toResponse(Terminal terminal) {
        return TerminalResponse.builder()
                .id(terminal.getId())
                .nombre(terminal.getNombre())
                .provincia(terminal.getProvincia())
                .canton(terminal.getCanton())
                .tipologia(terminal.getTipologia())
                .descripcionTipologia(terminal.getDescripcionTipologia())
                .andenes(terminal.getAndenes())
                .frecuenciasPorAnden(terminal.getFrecuenciasPorAnden())
                .maxFrecuenciasDiarias(terminal.getMaxFrecuenciasDiarias())
                .latitud(terminal.getLatitud())
                .longitud(terminal.getLongitud())
                .direccion(terminal.getDireccion())
                .telefono(terminal.getTelefono())
                .horarioApertura(terminal.getHorarioApertura())
                .horarioCierre(terminal.getHorarioCierre())
                .imagenUrl(terminal.getImagenUrl())
                .activo(terminal.getActivo())
                .build();
    }
}
