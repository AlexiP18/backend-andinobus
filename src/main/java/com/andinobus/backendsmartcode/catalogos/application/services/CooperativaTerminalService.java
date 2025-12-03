package com.andinobus.backendsmartcode.catalogos.application.services;

import com.andinobus.backendsmartcode.catalogos.api.dto.CooperativaTerminalDtos.*;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import com.andinobus.backendsmartcode.catalogos.domain.entities.CooperativaTerminal;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Terminal;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.CooperativaRepository;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.CooperativaTerminalRepository;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.TerminalRepository;
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
public class CooperativaTerminalService {

    private final CooperativaTerminalRepository cooperativaTerminalRepository;
    private final CooperativaRepository cooperativaRepository;
    private final TerminalRepository terminalRepository;

    /**
     * Obtiene todos los terminales asignados a una cooperativa
     */
    @Transactional(readOnly = true)
    public List<TerminalAsignadoResponse> getTerminalesByCooperativa(Long cooperativaId) {
        return cooperativaTerminalRepository.findByCooperativaIdWithTerminal(cooperativaId)
                .stream()
                .map(this::toTerminalAsignadoResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las cooperativas que operan en un terminal
     */
    @Transactional(readOnly = true)
    public List<CooperativaTerminalResponse> getCooperativasByTerminal(Long terminalId) {
        return cooperativaTerminalRepository.findByTerminalIdWithCooperativa(terminalId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Asigna un terminal a una cooperativa
     */
    @Transactional
    public CooperativaTerminalResponse asignarTerminal(Long cooperativaId, AsignarTerminalRequest request) {
        Cooperativa cooperativa = cooperativaRepository.findById(cooperativaId)
                .orElseThrow(() -> new RuntimeException("Cooperativa no encontrada: " + cooperativaId));

        Terminal terminal = terminalRepository.findById(request.getTerminalId())
                .orElseThrow(() -> new RuntimeException("Terminal no encontrado: " + request.getTerminalId()));

        // Verificar si ya existe la asignación
        var existente = cooperativaTerminalRepository.findByCooperativaIdAndTerminalId(cooperativaId, request.getTerminalId());
        if (existente.isPresent()) {
            CooperativaTerminal ct = existente.get();
            if (ct.getActivo()) {
                throw new RuntimeException("La cooperativa ya está asignada a este terminal");
            }
            // Reactivar asignación existente
            ct.setActivo(true);
            ct.setEsSedePrincipal(request.getEsSedePrincipal() != null ? request.getEsSedePrincipal() : false);
            ct.setNumeroAndenesAsignados(request.getNumeroAndenesAsignados() != null ? request.getNumeroAndenesAsignados() : 0);
            ct.setObservaciones(request.getObservaciones());
            return toResponse(cooperativaTerminalRepository.save(ct));
        }

        CooperativaTerminal ct = CooperativaTerminal.builder()
                .cooperativa(cooperativa)
                .terminal(terminal)
                .esSedePrincipal(request.getEsSedePrincipal() != null ? request.getEsSedePrincipal() : false)
                .numeroAndenesAsignados(request.getNumeroAndenesAsignados() != null ? request.getNumeroAndenesAsignados() : 0)
                .observaciones(request.getObservaciones())
                .activo(true)
                .build();

        log.info("Asignando terminal {} a cooperativa {}", terminal.getNombre(), cooperativa.getNombre());
        return toResponse(cooperativaTerminalRepository.save(ct));
    }

    /**
     * Asigna múltiples terminales a una cooperativa
     */
    @Transactional
    public List<CooperativaTerminalResponse> asignarTerminales(Long cooperativaId, AsignarTerminalesRequest request) {
        return request.getTerminalIds().stream()
                .map(terminalId -> {
                    try {
                        return asignarTerminal(cooperativaId, AsignarTerminalRequest.builder()
                                .terminalId(terminalId)
                                .esSedePrincipal(false)
                                .build());
                    } catch (Exception e) {
                        log.warn("Error asignando terminal {}: {}", terminalId, e.getMessage());
                        return null;
                    }
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza la asignación de un terminal
     */
    @Transactional
    public CooperativaTerminalResponse updateAsignacion(Long cooperativaId, Long terminalId, UpdateCooperativaTerminalRequest request) {
        CooperativaTerminal ct = cooperativaTerminalRepository.findByCooperativaIdAndTerminalId(cooperativaId, terminalId)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada"));

        if (request.getEsSedePrincipal() != null) ct.setEsSedePrincipal(request.getEsSedePrincipal());
        if (request.getNumeroAndenesAsignados() != null) ct.setNumeroAndenesAsignados(request.getNumeroAndenesAsignados());
        if (request.getObservaciones() != null) ct.setObservaciones(request.getObservaciones());

        return toResponse(cooperativaTerminalRepository.save(ct));
    }

    /**
     * Desasigna un terminal de una cooperativa
     */
    @Transactional
    public void desasignarTerminal(Long cooperativaId, Long terminalId) {
        CooperativaTerminal ct = cooperativaTerminalRepository.findByCooperativaIdAndTerminalId(cooperativaId, terminalId)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada"));
        
        ct.setActivo(false);
        cooperativaTerminalRepository.save(ct);
        log.info("Terminal {} desasignado de cooperativa {}", terminalId, cooperativaId);
    }

    /**
     * Sincroniza los terminales de una cooperativa (reemplaza todos)
     */
    @Transactional
    public List<TerminalAsignadoResponse> sincronizarTerminales(Long cooperativaId, List<Long> terminalIds) {
        // Desactivar todas las asignaciones actuales
        cooperativaTerminalRepository.findByCooperativaIdAndActivoTrue(cooperativaId)
                .forEach(ct -> {
                    ct.setActivo(false);
                    cooperativaTerminalRepository.save(ct);
                });

        // Crear nuevas asignaciones
        for (Long terminalId : terminalIds) {
            try {
                asignarTerminal(cooperativaId, AsignarTerminalRequest.builder()
                        .terminalId(terminalId)
                        .esSedePrincipal(false)
                        .build());
            } catch (Exception e) {
                log.warn("Error asignando terminal {}: {}", terminalId, e.getMessage());
            }
        }

        return getTerminalesByCooperativa(cooperativaId);
    }

    // ==================== MAPPERS ====================

    private CooperativaTerminalResponse toResponse(CooperativaTerminal ct) {
        return CooperativaTerminalResponse.builder()
                .id(ct.getId())
                .cooperativaId(ct.getCooperativa().getId())
                .cooperativaNombre(ct.getCooperativa().getNombre())
                .terminalId(ct.getTerminal().getId())
                .terminalNombre(ct.getTerminal().getNombre())
                .terminalCanton(ct.getTerminal().getCanton())
                .terminalProvincia(ct.getTerminal().getProvincia())
                .terminalTipologia(ct.getTerminal().getTipologia())
                .esSedePrincipal(ct.getEsSedePrincipal())
                .numeroAndenesAsignados(ct.getNumeroAndenesAsignados())
                .observaciones(ct.getObservaciones())
                .activo(ct.getActivo())
                .createdAt(ct.getCreatedAt())
                .build();
    }

    private TerminalAsignadoResponse toTerminalAsignadoResponse(CooperativaTerminal ct) {
        return TerminalAsignadoResponse.builder()
                .terminalId(ct.getTerminal().getId())
                .nombre(ct.getTerminal().getNombre())
                .canton(ct.getTerminal().getCanton())
                .provincia(ct.getTerminal().getProvincia())
                .tipologia(ct.getTerminal().getTipologia())
                .esSedePrincipal(ct.getEsSedePrincipal())
                .numeroAndenesAsignados(ct.getNumeroAndenesAsignados())
                .build();
    }
}
