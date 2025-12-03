package com.andinobus.backendsmartcode.catalogos.application.services;

import com.andinobus.backendsmartcode.catalogos.api.dto.UsuarioTerminalDtos.*;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Terminal;
import com.andinobus.backendsmartcode.catalogos.domain.entities.UsuarioTerminal;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.CooperativaRepository;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.TerminalRepository;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.UsuarioTerminalRepository;
import com.andinobus.backendsmartcode.usuarios.domain.entities.AppUser;
import com.andinobus.backendsmartcode.usuarios.domain.repositories.UserRepository;
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
public class UsuarioTerminalService {

    private final UsuarioTerminalRepository usuarioTerminalRepository;
    private final UserRepository userRepository;
    private final TerminalRepository terminalRepository;
    private final CooperativaRepository cooperativaRepository;

    /**
     * Obtiene todos los terminales asignados a un usuario (oficinista)
     */
    @Transactional(readOnly = true)
    public List<TerminalAsignadoUsuarioResponse> getTerminalesByUsuario(Long usuarioId) {
        return usuarioTerminalRepository.findByUsuarioIdWithTerminal(usuarioId)
                .stream()
                .map(this::toTerminalAsignadoResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los oficinistas que trabajan en un terminal
     */
    @Transactional(readOnly = true)
    public List<OficinistaPorTerminalResponse> getOficinistasByTerminal(Long terminalId) {
        return usuarioTerminalRepository.findByTerminalIdWithUsuario(terminalId)
                .stream()
                .map(this::toOficinistaPorTerminalResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los oficinistas de una cooperativa con sus terminales
     */
    @Transactional(readOnly = true)
    public List<UsuarioTerminalResponse> getOficinistasByCooperativa(Long cooperativaId) {
        return usuarioTerminalRepository.findByCooperativaIdWithUsuarioAndTerminal(cooperativaId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Asigna un terminal a un usuario (oficinista)
     */
    @Transactional
    public UsuarioTerminalResponse asignarTerminal(Long usuarioId, AsignarTerminalUsuarioRequest request) {
        AppUser usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + usuarioId));

        Terminal terminal = terminalRepository.findById(request.getTerminalId())
                .orElseThrow(() -> new RuntimeException("Terminal no encontrado: " + request.getTerminalId()));

        Cooperativa cooperativa = null;
        if (request.getCooperativaId() != null) {
            cooperativa = cooperativaRepository.findById(request.getCooperativaId())
                    .orElseThrow(() -> new RuntimeException("Cooperativa no encontrada: " + request.getCooperativaId()));
        }

        // Verificar si ya existe la asignación
        var existente = usuarioTerminalRepository.findByUsuarioIdAndTerminalId(usuarioId, request.getTerminalId());
        if (existente.isPresent()) {
            UsuarioTerminal ut = existente.get();
            if (ut.getActivo()) {
                throw new RuntimeException("El usuario ya está asignado a este terminal");
            }
            // Reactivar asignación existente
            ut.setActivo(true);
            ut.setCooperativa(cooperativa);
            ut.setCargo(request.getCargo() != null ? request.getCargo() : "Oficinista");
            ut.setTurno(request.getTurno());
            return toResponse(usuarioTerminalRepository.save(ut));
        }

        UsuarioTerminal ut = UsuarioTerminal.builder()
                .usuario(usuario)
                .terminal(terminal)
                .cooperativa(cooperativa)
                .cargo(request.getCargo() != null ? request.getCargo() : "Oficinista")
                .turno(request.getTurno())
                .activo(true)
                .build();

        log.info("Asignando terminal {} a usuario {}", terminal.getNombre(), usuario.getEmail());
        return toResponse(usuarioTerminalRepository.save(ut));
    }

    /**
     * Asigna múltiples terminales a un usuario
     */
    @Transactional
    public List<UsuarioTerminalResponse> asignarTerminales(Long usuarioId, AsignarTerminalesUsuarioRequest request) {
        return request.getTerminalIds().stream()
                .map(terminalId -> {
                    try {
                        return asignarTerminal(usuarioId, AsignarTerminalUsuarioRequest.builder()
                                .terminalId(terminalId)
                                .cooperativaId(request.getCooperativaId())
                                .cargo(request.getCargo())
                                .turno(request.getTurno())
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
     * Actualiza la asignación de un terminal a un usuario
     */
    @Transactional
    public UsuarioTerminalResponse updateAsignacion(Long usuarioId, Long terminalId, UpdateUsuarioTerminalRequest request) {
        UsuarioTerminal ut = usuarioTerminalRepository.findByUsuarioIdAndTerminalId(usuarioId, terminalId)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada"));

        if (request.getCooperativaId() != null) {
            Cooperativa cooperativa = cooperativaRepository.findById(request.getCooperativaId())
                    .orElseThrow(() -> new RuntimeException("Cooperativa no encontrada"));
            ut.setCooperativa(cooperativa);
        }
        if (request.getCargo() != null) ut.setCargo(request.getCargo());
        if (request.getTurno() != null) ut.setTurno(request.getTurno());

        return toResponse(usuarioTerminalRepository.save(ut));
    }

    /**
     * Desasigna un terminal de un usuario
     */
    @Transactional
    public void desasignarTerminal(Long usuarioId, Long terminalId) {
        UsuarioTerminal ut = usuarioTerminalRepository.findByUsuarioIdAndTerminalId(usuarioId, terminalId)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada"));
        
        ut.setActivo(false);
        usuarioTerminalRepository.save(ut);
        log.info("Terminal {} desasignado de usuario {}", terminalId, usuarioId);
    }

    /**
     * Sincroniza los terminales de un usuario (reemplaza todos)
     */
    @Transactional
    public List<TerminalAsignadoUsuarioResponse> sincronizarTerminales(Long usuarioId, AsignarTerminalesUsuarioRequest request) {
        // Desactivar todas las asignaciones actuales
        usuarioTerminalRepository.findByUsuarioIdAndActivoTrue(usuarioId)
                .forEach(ut -> {
                    ut.setActivo(false);
                    usuarioTerminalRepository.save(ut);
                });

        // Crear nuevas asignaciones
        for (Long terminalId : request.getTerminalIds()) {
            try {
                asignarTerminal(usuarioId, AsignarTerminalUsuarioRequest.builder()
                        .terminalId(terminalId)
                        .cooperativaId(request.getCooperativaId())
                        .cargo(request.getCargo())
                        .turno(request.getTurno())
                        .build());
            } catch (Exception e) {
                log.warn("Error asignando terminal {}: {}", terminalId, e.getMessage());
            }
        }

        return getTerminalesByUsuario(usuarioId);
    }

    // ==================== MAPPERS ====================

    private UsuarioTerminalResponse toResponse(UsuarioTerminal ut) {
        return UsuarioTerminalResponse.builder()
                .id(ut.getId())
                .usuarioId(ut.getUsuario().getId())
                .usuarioNombre(ut.getUsuario().getNombres() + " " + ut.getUsuario().getApellidos())
                .usuarioEmail(ut.getUsuario().getEmail())
                .terminalId(ut.getTerminal().getId())
                .terminalNombre(ut.getTerminal().getNombre())
                .terminalCanton(ut.getTerminal().getCanton())
                .terminalProvincia(ut.getTerminal().getProvincia())
                .cooperativaId(ut.getCooperativa() != null ? ut.getCooperativa().getId() : null)
                .cooperativaNombre(ut.getCooperativa() != null ? ut.getCooperativa().getNombre() : null)
                .cargo(ut.getCargo())
                .turno(ut.getTurno())
                .activo(ut.getActivo())
                .createdAt(ut.getCreatedAt())
                .build();
    }

    private TerminalAsignadoUsuarioResponse toTerminalAsignadoResponse(UsuarioTerminal ut) {
        return TerminalAsignadoUsuarioResponse.builder()
                .terminalId(ut.getTerminal().getId())
                .nombre(ut.getTerminal().getNombre())
                .canton(ut.getTerminal().getCanton())
                .provincia(ut.getTerminal().getProvincia())
                .tipologia(ut.getTerminal().getTipologia())
                .cargo(ut.getCargo())
                .turno(ut.getTurno())
                .cooperativaId(ut.getCooperativa() != null ? ut.getCooperativa().getId() : null)
                .cooperativaNombre(ut.getCooperativa() != null ? ut.getCooperativa().getNombre() : null)
                .build();
    }

    private OficinistaPorTerminalResponse toOficinistaPorTerminalResponse(UsuarioTerminal ut) {
        return OficinistaPorTerminalResponse.builder()
                .usuarioId(ut.getUsuario().getId())
                .nombres(ut.getUsuario().getNombres())
                .apellidos(ut.getUsuario().getApellidos())
                .email(ut.getUsuario().getEmail())
                .cargo(ut.getCargo())
                .turno(ut.getTurno())
                .cooperativaId(ut.getCooperativa() != null ? ut.getCooperativa().getId() : null)
                .cooperativaNombre(ut.getCooperativa() != null ? ut.getCooperativa().getNombre() : null)
                .build();
    }
}
