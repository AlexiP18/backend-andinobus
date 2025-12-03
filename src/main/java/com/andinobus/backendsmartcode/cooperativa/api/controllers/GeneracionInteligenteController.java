package com.andinobus.backendsmartcode.cooperativa.api.controllers;

import com.andinobus.backendsmartcode.cooperativa.api.dto.GeneracionInteligenteDtos.*;
import com.andinobus.backendsmartcode.cooperativa.application.services.GeneracionFrecuenciasInteligenteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * Controller para generación inteligente de frecuencias.
 * 
 * Características:
 * - Genera frecuencias basadas en disponibilidad real de buses
 * - Considera circuitos de ida y vuelta
 * - Respeta tiempos de descanso por tipo de frecuencia
 * - Maneja paradas intermedias solo para viajes interprovinciales
 */
@Slf4j
@RestController
@RequestMapping("/api/cooperativa/{cooperativaId}/frecuencias/generar-inteligente")
@RequiredArgsConstructor
@Tag(name = "Generación Inteligente", description = "APIs para generar frecuencias con lógica de negocio avanzada")
public class GeneracionInteligenteController {

    private final GeneracionFrecuenciasInteligenteService generacionService;

    @GetMapping("/estado")
    @Operation(summary = "Estado para generación inteligente", 
               description = "Retorna buses, terminales, rutas con cálculos automáticos de tipo y paradas")
    public ResponseEntity<EstadoGeneracionInteligente> getEstado(
            @PathVariable Long cooperativaId) {
        return ResponseEntity.ok(generacionService.getEstado(cooperativaId));
    }

    @PostMapping("/preview")
    @Operation(summary = "Preview de generación inteligente", 
               description = "Simula la generación considerando disponibilidad real de buses y circuitos")
    public ResponseEntity<PreviewGeneracionInteligente> preview(
            @PathVariable Long cooperativaId,
            @RequestBody GenerarInteligenteRequest request) {
        try {
            log.info("Preview request para cooperativa {}: fechaInicio={}, fechaFin={}, rutas={}", 
                    cooperativaId, request.getFechaInicio(), request.getFechaFin(), 
                    request.getRutasCircuito() != null ? request.getRutasCircuito().size() : 0);
            
            PreviewGeneracionInteligente resultado = generacionService.previewGeneracion(cooperativaId, request);
            log.info("Preview generado: {} frecuencias", resultado.getTotalFrecuencias());
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error en preview para cooperativa {}: {}", cooperativaId, e.getMessage(), e);
            // Retornar un preview vacío con el error
            return ResponseEntity.ok(PreviewGeneracionInteligente.builder()
                    .totalFrecuencias(0)
                    .frecuenciasPorDia(0)
                    .diasOperacion(0)
                    .busesUtilizados(0)
                    .busesDisponibles(0)
                    .frecuencias(Collections.emptyList())
                    .frecuenciasPorRuta(Collections.emptyMap())
                    .frecuenciasPorBus(Collections.emptyMap())
                    .advertencias(Collections.emptyList())
                    .errores(List.of("Error interno: " + e.getMessage()))
                    .esViable(false)
                    .build());
        }
    }

    @PostMapping
    @Operation(summary = "Generar frecuencias inteligentemente", 
               description = "Crea las frecuencias con lógica de circuitos y disponibilidad")
    public ResponseEntity<ResultadoGeneracionInteligente> generar(
            @PathVariable Long cooperativaId,
            @RequestBody GenerarInteligenteRequest request) {
        try {
            return ResponseEntity.ok(generacionService.generarFrecuencias(cooperativaId, request));
        } catch (Exception e) {
            log.error("Error generando frecuencias para cooperativa {}: {}", cooperativaId, e.getMessage(), e);
            return ResponseEntity.ok(ResultadoGeneracionInteligente.builder()
                    .exito(false)
                    .frecuenciasCreadas(0)
                    .mensajes(List.of("Error interno: " + e.getMessage()))
                    .advertencias(Collections.emptyList())
                    .build());
        }
    }
}
