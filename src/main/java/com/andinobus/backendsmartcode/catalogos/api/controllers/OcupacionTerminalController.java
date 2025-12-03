package com.andinobus.backendsmartcode.catalogos.api.controllers;

import com.andinobus.backendsmartcode.catalogos.application.services.OcupacionTerminalService;
import com.andinobus.backendsmartcode.catalogos.application.services.OcupacionTerminalService.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/terminales")
@RequiredArgsConstructor
public class OcupacionTerminalController {

    private final OcupacionTerminalService ocupacionService;

    /**
     * Obtener ocupación diaria de un terminal
     */
    @GetMapping("/{terminalId}/ocupacion")
    public ResponseEntity<OcupacionDiariaResponse> obtenerOcupacionDiaria(
            @PathVariable Long terminalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        
        if (fecha == null) fecha = LocalDate.now();
        return ResponseEntity.ok(ocupacionService.obtenerOcupacionDiaria(terminalId, fecha));
    }

    /**
     * Verificar disponibilidad para una hora específica
     */
    @GetMapping("/{terminalId}/disponibilidad")
    public ResponseEntity<Boolean> verificarDisponibilidad(
            @PathVariable Long terminalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora) {
        
        return ResponseEntity.ok(ocupacionService.tieneDisponibilidad(terminalId, fecha, hora));
    }

    /**
     * Verificar disponibilidad en un rango de horas
     */
    @GetMapping("/{terminalId}/disponibilidad/rango")
    public ResponseEntity<DisponibilidadRangoResponse> verificarDisponibilidadRango(
            @PathVariable Long terminalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin) {
        
        return ResponseEntity.ok(ocupacionService.verificarDisponibilidadRango(
                terminalId, fecha, horaInicio, horaFin));
    }

    /**
     * Obtener sugerencias de horarios óptimos (menos congestionados)
     */
    @GetMapping("/{terminalId}/sugerencias")
    public ResponseEntity<List<SugerenciaHoraResponse>> obtenerSugerencias(
            @PathVariable Long terminalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(defaultValue = "10") int cantidad) {
        
        if (fecha == null) fecha = LocalDate.now();
        return ResponseEntity.ok(ocupacionService.sugerirHorasOptimas(terminalId, fecha, cantidad));
    }

    /**
     * Registrar una frecuencia en un terminal (usado internamente al crear frecuencias)
     */
    @PostMapping("/{terminalId}/frecuencia")
    public ResponseEntity<Void> registrarFrecuencia(
            @PathVariable Long terminalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora) {
        
        ocupacionService.registrarFrecuencia(terminalId, fecha, hora);
        return ResponseEntity.ok().build();
    }

    /**
     * Eliminar una frecuencia de un terminal
     */
    @DeleteMapping("/{terminalId}/frecuencia")
    public ResponseEntity<Void> eliminarFrecuencia(
            @PathVariable Long terminalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora) {
        
        ocupacionService.eliminarFrecuencia(terminalId, fecha, hora);
        return ResponseEntity.ok().build();
    }
}
