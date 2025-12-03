package com.andinobus.backendsmartcode.catalogos.application.services;

import com.andinobus.backendsmartcode.catalogos.api.dto.BusChoferDtos.*;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.catalogos.domain.entities.BusChofer;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusChoferRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusRepository;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.UsuarioCooperativa;
import com.andinobus.backendsmartcode.cooperativa.domain.enums.RolCooperativa;
import com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories.UsuarioCooperativaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusChoferService {

    private static final int MAX_CHOFERES_POR_BUS = 3;

    private final BusChoferRepository busChoferRepository;
    private final BusRepository busRepository;
    private final UsuarioCooperativaRepository usuarioCooperativaRepository;

    /**
     * Obtener todos los choferes asignados a un bus
     */
    @Transactional(readOnly = true)
    public List<BusChoferResponse> getChoferesDelBus(Long cooperativaId, Long busId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new EntityNotFoundException("Bus no encontrado"));

        if (!bus.getCooperativa().getId().equals(cooperativaId)) {
            throw new IllegalArgumentException("El bus no pertenece a la cooperativa");
        }

        return busChoferRepository.findByBusIdAndActivoTrueOrderByOrdenAsc(busId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los choferes disponibles de una cooperativa
     */
    @Transactional(readOnly = true)
    public List<ChoferDisponible> getChoferesDisponibles(Long cooperativaId, Long busId) {
        // Obtener todos los choferes de la cooperativa
        List<UsuarioCooperativa> choferes = usuarioCooperativaRepository
                .findChoferesActivosByCooperativa(cooperativaId);

        // Obtener asignaciones actuales del bus
        List<BusChofer> asignacionesBus = busChoferRepository.findByBusIdAndActivoTrueOrderByOrdenAsc(busId);
        List<Long> choferesAsignadosBus = asignacionesBus.stream()
                .map(bc -> bc.getChofer().getId())
                .collect(Collectors.toList());

        // Obtener todas las asignaciones activas de la cooperativa
        List<BusChofer> todasAsignaciones = busChoferRepository.findByCooperativaId(cooperativaId);

        return choferes.stream()
                .map(chofer -> {
                    // Verificar si está asignado a otro bus
                    BusChofer asignacionOtroBus = todasAsignaciones.stream()
                            .filter(bc -> bc.getChofer().getId().equals(chofer.getId()) && !bc.getBus().getId().equals(busId))
                            .findFirst()
                            .orElse(null);

                    return ChoferDisponible.builder()
                            .id(chofer.getId())
                            .nombres(chofer.getNombres())
                            .apellidos(chofer.getApellidos())
                            .nombreCompleto(chofer.getNombres() + " " + chofer.getApellidos())
                            .cedula(chofer.getCedula())
                            .telefono(chofer.getTelefono())
                            .email(chofer.getEmail())
                            .fotoUrl(chofer.getFotoUrl())
                            .numeroLicencia(chofer.getLicenciaConducir())
                            .tipoLicencia(chofer.getTipoLicencia())
                            .fechaVencimientoLicencia(chofer.getFechaVencimientoLicencia() != null 
                                    ? chofer.getFechaVencimientoLicencia().toString() : null)
                            .yaAsignado(choferesAsignadosBus.contains(chofer.getId()))
                            .busAsignadoPlaca(asignacionOtroBus != null ? asignacionOtroBus.getBus().getPlaca() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Asignar un chofer a un bus
     */
    @Transactional
    public BusChoferResponse asignarChofer(Long cooperativaId, Long busId, AsignarChoferRequest request) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new EntityNotFoundException("Bus no encontrado"));

        if (!bus.getCooperativa().getId().equals(cooperativaId)) {
            throw new IllegalArgumentException("El bus no pertenece a la cooperativa");
        }

        // Verificar límite de choferes
        long cantidadActual = busChoferRepository.countActiveByBusId(busId);
        if (cantidadActual >= MAX_CHOFERES_POR_BUS) {
            throw new IllegalStateException("El bus ya tiene el máximo de " + MAX_CHOFERES_POR_BUS + " choferes asignados");
        }

        UsuarioCooperativa chofer = usuarioCooperativaRepository.findById(request.getChoferId())
                .orElseThrow(() -> new EntityNotFoundException("Chofer no encontrado"));

        if (!chofer.getCooperativa().getId().equals(cooperativaId)) {
            throw new IllegalArgumentException("El chofer no pertenece a la cooperativa");
        }

        if (!RolCooperativa.CHOFER.equals(chofer.getRolCooperativa())) {
            throw new IllegalArgumentException("El usuario no tiene rol de chofer");
        }

        // Verificar si ya está asignado a este bus
        if (busChoferRepository.findByBusIdAndChoferId(busId, request.getChoferId()).isPresent()) {
            throw new IllegalStateException("El chofer ya está asignado a este bus");
        }

        // Si es tipo PRINCIPAL, verificar que no haya otro principal
        if ("PRINCIPAL".equals(request.getTipo())) {
            busChoferRepository.findPrincipalByBusId(busId).ifPresent(bc -> {
                throw new IllegalStateException("El bus ya tiene un chofer principal asignado");
            });
        }

        // Determinar orden
        int orden = (int) cantidadActual + 1;
        if ("PRINCIPAL".equals(request.getTipo())) {
            orden = 1;
            // Reordenar los existentes
            busChoferRepository.findByBusIdAndActivoTrueOrderByOrdenAsc(busId).forEach(bc -> {
                bc.setOrden(bc.getOrden() + 1);
                busChoferRepository.save(bc);
            });
        }

        BusChofer busChofer = BusChofer.builder()
                .bus(bus)
                .chofer(chofer)
                .tipo(request.getTipo() != null ? request.getTipo() : "ALTERNO")
                .orden(orden)
                .activo(true)
                .build();

        return toResponse(busChoferRepository.save(busChofer));
    }

    /**
     * Sincronizar choferes de un bus (reemplaza todas las asignaciones)
     */
    @Transactional
    public List<BusChoferResponse> sincronizarChoferes(Long cooperativaId, Long busId, SincronizarChoferesRequest request) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new EntityNotFoundException("Bus no encontrado"));

        if (!bus.getCooperativa().getId().equals(cooperativaId)) {
            throw new IllegalArgumentException("El bus no pertenece a la cooperativa");
        }

        if (request.getChoferes().size() > MAX_CHOFERES_POR_BUS) {
            throw new IllegalArgumentException("No se pueden asignar más de " + MAX_CHOFERES_POR_BUS + " choferes");
        }

        // Verificar que solo hay un PRINCIPAL
        long principalCount = request.getChoferes().stream()
                .filter(c -> "PRINCIPAL".equals(c.getTipo()))
                .count();
        if (principalCount > 1) {
            throw new IllegalArgumentException("Solo puede haber un chofer principal por bus");
        }

        // Desactivar asignaciones actuales
        List<BusChofer> asignacionesActuales = busChoferRepository.findByBusIdAndActivoTrueOrderByOrdenAsc(busId);
        asignacionesActuales.forEach(bc -> {
            bc.setActivo(false);
            busChoferRepository.save(bc);
        });

        // Crear nuevas asignaciones
        List<BusChoferResponse> responses = new ArrayList<>();
        int orden = 1;

        for (ChoferAsignacion asignacion : request.getChoferes()) {
            UsuarioCooperativa chofer = usuarioCooperativaRepository.findById(asignacion.getChoferId())
                    .orElseThrow(() -> new EntityNotFoundException("Chofer " + asignacion.getChoferId() + " no encontrado"));

            if (!chofer.getCooperativa().getId().equals(cooperativaId)) {
                throw new IllegalArgumentException("El chofer " + chofer.getId() + " no pertenece a la cooperativa");
            }

            if (!RolCooperativa.CHOFER.equals(chofer.getRolCooperativa())) {
                throw new IllegalArgumentException("El usuario " + chofer.getId() + " no tiene rol de chofer");
            }

            // Verificar si ya existe la asignación (desactivada)
            BusChofer busChofer = busChoferRepository.findByBusIdAndChoferId(busId, asignacion.getChoferId())
                    .orElse(BusChofer.builder()
                            .bus(bus)
                            .chofer(chofer)
                            .build());

            busChofer.setTipo(asignacion.getTipo() != null ? asignacion.getTipo() : "ALTERNO");
            busChofer.setOrden(asignacion.getOrden() != null ? asignacion.getOrden() : orden++);
            busChofer.setActivo(true);

            responses.add(toResponse(busChoferRepository.save(busChofer)));
        }

        return responses;
    }

    /**
     * Remover un chofer de un bus
     */
    @Transactional
    public void removerChofer(Long cooperativaId, Long busId, Long choferId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new EntityNotFoundException("Bus no encontrado"));

        if (!bus.getCooperativa().getId().equals(cooperativaId)) {
            throw new IllegalArgumentException("El bus no pertenece a la cooperativa");
        }

        BusChofer busChofer = busChoferRepository.findByBusIdAndChoferId(busId, choferId)
                .orElseThrow(() -> new EntityNotFoundException("Asignación no encontrada"));

        busChofer.setActivo(false);
        busChoferRepository.save(busChofer);

        // Reordenar los restantes
        List<BusChofer> restantes = busChoferRepository.findByBusIdAndActivoTrueOrderByOrdenAsc(busId);
        int orden = 1;
        for (BusChofer bc : restantes) {
            bc.setOrden(orden++);
            busChoferRepository.save(bc);
        }
    }

    private BusChoferResponse toResponse(BusChofer bc) {
        return BusChoferResponse.builder()
                .id(bc.getId())
                .busId(bc.getBus().getId())
                .busPlaca(bc.getBus().getPlaca())
                .busNumeroInterno(bc.getBus().getNumeroInterno())
                .choferId(bc.getChofer().getId())
                .choferNombre(bc.getChofer().getNombres() + " " + bc.getChofer().getApellidos())
                .choferCedula(bc.getChofer().getCedula())
                .choferTelefono(bc.getChofer().getTelefono())
                .choferFotoUrl(bc.getChofer().getFotoUrl())
                .tipo(bc.getTipo())
                .orden(bc.getOrden())
                .activo(bc.getActivo())
                .createdAt(bc.getCreatedAt())
                .build();
    }
}
