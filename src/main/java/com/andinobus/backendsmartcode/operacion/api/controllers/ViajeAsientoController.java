package com.andinobus.backendsmartcode.operacion.api.controllers;

import com.andinobus.backendsmartcode.operacion.api.dto.ViajeAsientoDtos;
import com.andinobus.backendsmartcode.operacion.application.services.ViajeAsientoService;
import com.andinobus.backendsmartcode.operacion.domain.entities.ViajeAsiento;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/viajes")
@Profile("dev")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ViajeAsientoController {

    private final ViajeAsientoService viajeAsientoService;

    /**
     * Obtiene todos los asientos de un viaje con su estado actual.
     * GET /api/viajes/{viajeId}/asientos
     */
    @GetMapping("/{viajeId}/asientos")
    public ResponseEntity<List<ViajeAsientoDtos.AsientoDisponibilidadResponse>> obtenerAsientosViaje(
            @PathVariable Long viajeId) {
        
        List<ViajeAsiento> asientos = viajeAsientoService.obtenerAsientosViaje(viajeId);
        
        List<ViajeAsientoDtos.AsientoDisponibilidadResponse> response = asientos.stream()
                .map(this::toAsientoResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene solo los asientos disponibles de un viaje.
     * GET /api/viajes/{viajeId}/asientos/disponibles
     */
    @GetMapping("/{viajeId}/asientos/disponibles")
    public ResponseEntity<List<ViajeAsientoDtos.AsientoDisponibilidadResponse>> obtenerAsientosDisponibles(
            @PathVariable Long viajeId) {
        
        List<ViajeAsiento> asientos = viajeAsientoService.obtenerAsientosDisponibles(viajeId);
        
        List<ViajeAsientoDtos.AsientoDisponibilidadResponse> response = asientos.stream()
                .map(this::toAsientoResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene estadísticas de ocupación de un viaje.
     * GET /api/viajes/{viajeId}/asientos/estadisticas
     */
    @GetMapping("/{viajeId}/asientos/estadisticas")
    public ResponseEntity<ViajeAsientoDtos.AsientosEstadisticasResponse> obtenerEstadisticas(
            @PathVariable Long viajeId) {
        
        ViajeAsientoService.AsientosEstadisticas stats = viajeAsientoService.obtenerEstadisticas(viajeId);
        
        ViajeAsientoDtos.AsientosEstadisticasResponse response = ViajeAsientoDtos.AsientosEstadisticasResponse.builder()
                .total(stats.total())
                .disponibles(stats.disponibles())
                .reservados(stats.reservados())
                .vendidos(stats.vendidos())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Inicializa los asientos de un viaje basándose en el layout del bus.
     * POST /api/viajes/{viajeId}/asientos/inicializar
     */
    @PostMapping("/{viajeId}/asientos/inicializar")
    public ResponseEntity<ViajeAsientoDtos.InicializarAsientosResponse> inicializarAsientos(
            @PathVariable Long viajeId) {
        
        viajeAsientoService.inicializarAsientosViaje(viajeId);
        
        // Obtener estadísticas después de inicializar
        ViajeAsientoService.AsientosEstadisticas stats = viajeAsientoService.obtenerEstadisticas(viajeId);
        
        ViajeAsientoDtos.InicializarAsientosResponse response = ViajeAsientoDtos.InicializarAsientosResponse.builder()
                .success(true)
                .mensaje("Asientos inicializados correctamente")
                .asientosCreados((int) stats.total())
                .build();
        
        return ResponseEntity.ok(response);
    }

    private ViajeAsientoDtos.AsientoDisponibilidadResponse toAsientoResponse(ViajeAsiento asiento) {
        return ViajeAsientoDtos.AsientoDisponibilidadResponse.builder()
                .numeroAsiento(asiento.getNumeroAsiento())
                .tipoAsiento(asiento.getTipoAsiento())
                .estado(asiento.getEstado())
                .build();
    }
}
