package com.andinobus.backendsmartcode.cooperativa.api.controllers;

import com.andinobus.backendsmartcode.cooperativa.api.dto.FrecuenciaConfigDtos.*;
import com.andinobus.backendsmartcode.cooperativa.application.services.FrecuenciaDisponibilidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cooperativa/{cooperativaId}/frecuencias")
@RequiredArgsConstructor
@Tag(name = "Frecuencias - Cooperativa", description = "Gestión de frecuencias para cooperativas")
public class FrecuenciaCooperativaController {

    private final FrecuenciaDisponibilidadService disponibilidadService;

    @GetMapping("/config")
    @Operation(summary = "Obtener configuración de frecuencias de la cooperativa")
    public ResponseEntity<FrecuenciaConfigResponse> getConfiguracion(
            @PathVariable Long cooperativaId) {
        return ResponseEntity.ok(disponibilidadService.getConfiguracion(cooperativaId));
    }

    @PutMapping("/config")
    @Operation(summary = "Actualizar configuración de frecuencias")
    public ResponseEntity<FrecuenciaConfigResponse> updateConfiguracion(
            @PathVariable Long cooperativaId,
            @RequestBody UpdateFrecuenciaConfigRequest request) {
        return ResponseEntity.ok(disponibilidadService.updateConfiguracion(cooperativaId, request));
    }

    @GetMapping("/rutas-disponibles")
    @Operation(summary = "Obtener rutas disponibles basadas en terminales de la cooperativa")
    public ResponseEntity<List<RutaDisponibleResponse>> getRutasDisponibles(
            @PathVariable Long cooperativaId) {
        return ResponseEntity.ok(disponibilidadService.getRutasDisponibles(cooperativaId));
    }

    @GetMapping("/buses/disponibilidad")
    @Operation(summary = "Obtener disponibilidad de buses para una fecha")
    public ResponseEntity<List<BusDisponibilidadResponse>> getBusesDisponibles(
            @PathVariable Long cooperativaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(disponibilidadService.getBusesDisponibles(cooperativaId, fecha));
    }

    @GetMapping("/choferes/disponibilidad")
    @Operation(summary = "Obtener disponibilidad de choferes para una fecha")
    public ResponseEntity<List<ChoferDisponibilidadResponse>> getChoferesDisponibles(
            @PathVariable Long cooperativaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(disponibilidadService.getChoferesDisponibles(cooperativaId, fecha));
    }

    @PostMapping("/validar")
    @Operation(summary = "Validar si se puede crear una frecuencia con los datos proporcionados")
    public ResponseEntity<ValidacionFrecuenciaResponse> validarFrecuencia(
            @PathVariable Long cooperativaId,
            @RequestBody CrearFrecuenciaValidadaRequest request,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(disponibilidadService.validarFrecuencia(cooperativaId, request, fecha));
    }
}
