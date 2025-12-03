package com.andinobus.backendsmartcode.tracking.application.controllers;

import com.andinobus.backendsmartcode.tracking.application.dto.ViajeActivoDTO;
import com.andinobus.backendsmartcode.tracking.domain.services.ViajeTrackingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para consultar viajes activos y su estado
 * Usado por los dashboards para mostrar los viajes en curso
 */
@RestController
@RequestMapping("/api/viajes")
@RequiredArgsConstructor
public class ViajeQueryController {

    private final ViajeTrackingQueryService viajeQueryService;

    /**
     * Obtener todos los viajes activos del sistema (Super Admin)
     */
    @GetMapping("/activos")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<ViajeActivoDTO>> obtenerViajesActivosGlobal() {
        List<ViajeActivoDTO> viajes = viajeQueryService.obtenerViajesActivos();
        return ResponseEntity.ok(viajes);
    }

    /**
     * Obtener viajes activos de una cooperativa específica
     */
    @GetMapping("/activos/cooperativa/{cooperativaId}")
    @PreAuthorize("hasAnyRole('ADMIN_COOPERATIVA', 'SUPER_ADMIN')")
    public ResponseEntity<List<ViajeActivoDTO>> obtenerViajesActivosPorCooperativa(
            @PathVariable Long cooperativaId) {
        List<ViajeActivoDTO> viajes = viajeQueryService.obtenerViajesActivosPorCooperativa(cooperativaId);
        return ResponseEntity.ok(viajes);
    }

    /**
     * Obtener viajes activos de un cliente (viajes de boletos comprados)
     */
    @GetMapping("/activos/cliente")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<ViajeActivoDTO>> obtenerViajesActivosCliente(
            @RequestParam String email) {
        List<ViajeActivoDTO> viajes = viajeQueryService.obtenerViajesActivosPorCliente(email);
        return ResponseEntity.ok(viajes);
    }

    /**
     * Obtener información detallada de un viaje específico
     */
    @GetMapping("/{viajeId}/detalle")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN_COOPERATIVA', 'SUPER_ADMIN', 'CHOFER')")
    public ResponseEntity<ViajeActivoDTO> obtenerDetalleViaje(@PathVariable Long viajeId) {
        ViajeActivoDTO viaje = viajeQueryService.obtenerDetalleViaje(viajeId);
        return ResponseEntity.ok(viaje);
    }
}
