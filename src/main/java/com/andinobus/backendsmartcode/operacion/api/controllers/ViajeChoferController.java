package com.andinobus.backendsmartcode.operacion.api.controllers;

import com.andinobus.backendsmartcode.operacion.api.dto.ViajeChoferDtos.*;
import com.andinobus.backendsmartcode.operacion.application.services.ViajeChoferService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para gestión de viajes desde la perspectiva del chofer
 */
@RestController
@RequestMapping("/api/chofer")
@Profile("dev")
@RequiredArgsConstructor
public class ViajeChoferController {

    private final ViajeChoferService viajeChoferService;

    /**
     * Obtiene el viaje del día del chofer con lista de pasajeros
     * 
     * GET /api/chofer/{choferId}/viaje?fecha=2025-01-20
     */
    @GetMapping("/{choferId}/viaje")
    public ResponseEntity<ViajeChoferResponse> getViajeDelDia(
            @PathVariable Long choferId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        LocalDate fechaBusqueda = fecha != null ? fecha : LocalDate.now();
        ViajeChoferResponse viaje = viajeChoferService.getViajeDelDia(choferId, fechaBusqueda);
        
        if (viaje == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(viaje);
    }

    /**
     * Inicia un viaje (cambia estado a EN_RUTA)
     * 
     * POST /api/chofer/viaje/{viajeId}/iniciar
     */
    @PostMapping("/viaje/{viajeId}/iniciar")
    public ResponseEntity<ViajeOperacionResponse> iniciarViaje(
            @PathVariable Long viajeId,
            @RequestBody(required = false) IniciarViajeRequest request
    ) {
        if (request == null) {
            request = new IniciarViajeRequest(null);
        }
        
        ViajeOperacionResponse response = viajeChoferService.iniciarViaje(viajeId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Finaliza un viaje (cambia estado a COMPLETADO)
     * 
     * POST /api/chofer/viaje/{viajeId}/finalizar
     */
    @PostMapping("/viaje/{viajeId}/finalizar")
    public ResponseEntity<ViajeOperacionResponse> finalizarViaje(
            @PathVariable Long viajeId,
            @RequestBody(required = false) FinalizarViajeRequest request
    ) {
        if (request == null) {
            request = new FinalizarViajeRequest(null, null);
        }
        
        ViajeOperacionResponse response = viajeChoferService.finalizarViaje(viajeId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el historial de viajes completados del chofer
     * 
     * GET /api/chofer/{choferId}/historial
     */
    @GetMapping("/{choferId}/historial")
    public ResponseEntity<List<ViajeHistorialResponse>> getHistorialViajes(
            @PathVariable Long choferId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        List<ViajeHistorialResponse> historial;
        
        if (fechaInicio != null && fechaFin != null) {
            historial = viajeChoferService.getHistorialViajesByFechas(choferId, fechaInicio, fechaFin);
        } else {
            historial = viajeChoferService.getHistorialViajes(choferId);
        }
        
        return ResponseEntity.ok(historial);
    }

    /**
     * Obtiene las calificaciones de un viaje específico
     * 
     * GET /api/chofer/viaje/{viajeId}/calificaciones
     */
    @GetMapping("/viaje/{viajeId}/calificaciones")
    public ResponseEntity<List<CalificacionResponse>> getCalificacionesViaje(@PathVariable Long viajeId) {
        List<CalificacionResponse> calificaciones = viajeChoferService.getCalificacionesViaje(viajeId);
        return ResponseEntity.ok(calificaciones);
    }

    /**
     * Obtiene todas las calificaciones del chofer con promedio
     * 
     * GET /api/chofer/{choferId}/calificaciones
     */
    @GetMapping("/{choferId}/calificaciones")
    public ResponseEntity<CalificacionesChoferResponse> getCalificacionesChofer(@PathVariable Long choferId) {
        CalificacionesChoferResponse calificaciones = viajeChoferService.getCalificacionesChofer(choferId);
        return ResponseEntity.ok(calificaciones);
    }

    /**
     * Obtiene las rutas (frecuencias) asignadas a la cooperativa del chofer
     * 
     * GET /api/chofer/{choferId}/mis-rutas
     */
    @GetMapping("/{choferId}/mis-rutas")
    public ResponseEntity<List<RutaChoferResponse>> getMisRutas(@PathVariable Long choferId) {
        List<RutaChoferResponse> rutas = viajeChoferService.getMisRutas(choferId);
        return ResponseEntity.ok(rutas);
    }
}
