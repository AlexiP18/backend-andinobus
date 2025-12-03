package com.andinobus.backendsmartcode.cooperativa.api.controllers;

import com.andinobus.backendsmartcode.cooperativa.api.dto.CapacidadOperativaDtos.*;
import com.andinobus.backendsmartcode.cooperativa.application.services.CapacidadOperativaService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller para gestionar la capacidad operativa de la cooperativa.
 * Permite calcular cuántas frecuencias puede manejar según recursos disponibles.
 */
@RestController
@RequestMapping("/api/cooperativa/{cooperativaId}/capacidad")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Profile("dev")
public class CapacidadOperativaController {

    private final CapacidadOperativaService capacidadService;

    /**
     * Obtener capacidad operativa actual de la cooperativa
     */
    @GetMapping
    public ResponseEntity<CapacidadOperativaResponse> obtenerCapacidad(
            @PathVariable Long cooperativaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return ResponseEntity.ok(capacidadService.calcularCapacidad(cooperativaId, fecha));
    }

    /**
     * Obtener resumen de capacidad para el modal de generación
     */
    @GetMapping("/resumen")
    public ResponseEntity<ResumenCapacidadModal> obtenerResumenModal(
            @PathVariable Long cooperativaId,
            @RequestParam(defaultValue = "1") int semanas
    ) {
        return ResponseEntity.ok(capacidadService.obtenerResumenParaModal(cooperativaId, semanas));
    }

    /**
     * Validar si se puede generar un número específico de frecuencias
     */
    @GetMapping("/validar")
    public ResponseEntity<ValidacionGeneracionResponse> validarGeneracion(
            @PathVariable Long cooperativaId,
            @RequestParam int frecuenciasSolicitadas,
            @RequestParam(defaultValue = "1") int semanas
    ) {
        return ResponseEntity.ok(
                capacidadService.validarGeneracion(cooperativaId, frecuenciasSolicitadas, semanas)
        );
    }

    /**
     * Obtener capacidad desglosada por semana
     */
    @GetMapping("/semanal")
    public ResponseEntity<CapacidadSemanalResponse> obtenerCapacidadSemanal(
            @PathVariable Long cooperativaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(defaultValue = "1") int semana
    ) {
        return ResponseEntity.ok(
                capacidadService.calcularCapacidadSemanal(cooperativaId, fechaInicio, semana)
        );
    }
}
