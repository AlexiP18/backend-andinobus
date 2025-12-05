package com.andinobus.backendsmartcode.cooperativa.api.controllers;

import com.andinobus.backendsmartcode.cooperativa.api.dto.ReporteCooperativaDtos.*;
import com.andinobus.backendsmartcode.cooperativa.application.services.ReporteCooperativaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/cooperativa/{cooperativaId}/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes Cooperativa", description = "Endpoints para reportes y estadísticas de cooperativa")
public class ReporteCooperativaController {

    private final ReporteCooperativaService reporteService;

    @Operation(summary = "Obtener resumen general", 
            description = "Obtiene un resumen consolidado de ventas, viajes, ocupación y recursos")
    @GetMapping("/resumen")
    public ResponseEntity<ResumenCooperativaResponse> obtenerResumen(
            @PathVariable Long cooperativaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteService.obtenerResumenGeneral(cooperativaId, fechaInicio, fechaFin));
    }

    @Operation(summary = "Obtener reporte de ventas", 
            description = "Obtiene estadísticas detalladas de ventas por día y por ruta")
    @GetMapping("/ventas")
    public ResponseEntity<ReporteVentasResponse> obtenerReporteVentas(
            @PathVariable Long cooperativaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteService.obtenerReporteVentas(cooperativaId, fechaInicio, fechaFin));
    }

    @Operation(summary = "Obtener reporte de viajes", 
            description = "Obtiene estadísticas detalladas de viajes por estado, día, ruta y bus")
    @GetMapping("/viajes")
    public ResponseEntity<ReporteViajesResponse> obtenerReporteViajes(
            @PathVariable Long cooperativaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteService.obtenerReporteViajes(cooperativaId, fechaInicio, fechaFin));
    }

    @Operation(summary = "Obtener reporte de ocupación", 
            description = "Obtiene estadísticas detalladas de ocupación por día, ruta y hora")
    @GetMapping("/ocupacion")
    public ResponseEntity<ReporteOcupacionResponse> obtenerReporteOcupacion(
            @PathVariable Long cooperativaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteService.obtenerReporteOcupacion(cooperativaId, fechaInicio, fechaFin));
    }

    @Operation(summary = "Obtener reporte de rutas", 
            description = "Obtiene estadísticas detalladas por cada ruta de la cooperativa")
    @GetMapping("/rutas")
    public ResponseEntity<ReporteRutasResponse> obtenerReporteRutas(
            @PathVariable Long cooperativaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteService.obtenerReporteRutas(cooperativaId, fechaInicio, fechaFin));
    }
}
