package com.andinobus.backendsmartcode.cooperativa.api.controllers;

import com.andinobus.backendsmartcode.cooperativa.api.dto.GeneracionAutomaticaDtos.*;
import com.andinobus.backendsmartcode.cooperativa.application.services.GeneracionAutomaticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cooperativa/{cooperativaId}/frecuencias/generar-automatico")
@RequiredArgsConstructor
@Tag(name = "Generación Automática de Frecuencias", description = "APIs para generar frecuencias automáticamente basado en reglas de negocio")
public class GeneracionAutomaticaController {

    private final GeneracionAutomaticaService generacionService;

    @GetMapping("/estado")
    @Operation(summary = "Obtener estado actual para generación", 
               description = "Retorna buses disponibles, choferes, rutas y configuración")
    public ResponseEntity<EstadoGeneracion> getEstadoGeneracion(
            @PathVariable Long cooperativaId) {
        return ResponseEntity.ok(generacionService.getEstadoGeneracion(cooperativaId));
    }

    @PostMapping("/preview")
    @Operation(summary = "Vista previa de generación", 
               description = "Muestra las frecuencias que se crearían sin guardarlas")
    public ResponseEntity<PreviewAutomaticoResponse> previewGeneracion(
            @PathVariable Long cooperativaId,
            @RequestBody GenerarAutomaticoRequest request) {
        return ResponseEntity.ok(generacionService.previewGeneracion(cooperativaId, request));
    }

    @PostMapping
    @Operation(summary = "Generar frecuencias", 
               description = "Crea las frecuencias en la base de datos")
    public ResponseEntity<ResultadoGeneracionAutomatica> generarFrecuencias(
            @PathVariable Long cooperativaId,
            @RequestBody GenerarAutomaticoRequest request) {
        return ResponseEntity.ok(generacionService.generarFrecuencias(cooperativaId, request));
    }
}
