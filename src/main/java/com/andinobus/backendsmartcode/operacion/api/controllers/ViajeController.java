package com.andinobus.backendsmartcode.operacion.api.controllers;

import com.andinobus.backendsmartcode.operacion.api.dto.ViajeDtos.*;
import com.andinobus.backendsmartcode.operacion.application.services.ViajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para gestión de viajes disponibles
 */
@RestController
@RequestMapping("/api/cooperativa/{cooperativaId}/viajes")
@Profile("dev")
@RequiredArgsConstructor
public class ViajeController {

    private final ViajeService viajeService;

    /**
     * Lista los viajes disponibles de una cooperativa para una fecha
     * 
     * GET /api/cooperativa/{cooperativaId}/viajes?fecha=2025-01-20
     */
    @GetMapping
    public ResponseEntity<List<ViajeDisponibleResponse>> getViajesDisponibles(
            @PathVariable Long cooperativaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        List<ViajeDisponibleResponse> viajes = viajeService.getViajesDisponibles(cooperativaId, fecha);
        return ResponseEntity.ok(viajes);
    }

    /**
     * Obtiene el detalle de un viaje con información de asientos
     * 
     * GET /api/cooperativa/{cooperativaId}/viajes/{viajeId}
     */
    @GetMapping("/{viajeId}")
    public ResponseEntity<ViajeDetalleResponse> getViajeDetalle(
            @PathVariable Long cooperativaId,
            @PathVariable Long viajeId
    ) {
        ViajeDetalleResponse viaje = viajeService.getViajeDetalle(viajeId);
        return ResponseEntity.ok(viaje);
    }
}
