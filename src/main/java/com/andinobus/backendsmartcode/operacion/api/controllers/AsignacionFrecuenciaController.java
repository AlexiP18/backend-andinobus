package com.andinobus.backendsmartcode.operacion.api.controllers;

import com.andinobus.backendsmartcode.operacion.application.services.AsignacionFrecuenciaService;
import com.andinobus.backendsmartcode.operacion.application.services.AsignacionFrecuenciaService.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/asignaciones")
@RequiredArgsConstructor
public class AsignacionFrecuenciaController {

    private final AsignacionFrecuenciaService asignacionService;

    /**
     * Validar si un chofer puede ser asignado a un viaje
     */
    @GetMapping("/validar/chofer/{choferId}")
    @PreAuthorize("hasAnyRole('COOPERATIVA', 'ADMIN', 'OFICINISTA')")
    public ResponseEntity<ValidacionChoferResponse> validarChofer(
            @PathVariable Long choferId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam int duracionMinutos) {
        
        return ResponseEntity.ok(asignacionService.validarAsignacionChofer(choferId, fecha, duracionMinutos));
    }

    /**
     * Validar disponibilidad de terminales para una frecuencia
     */
    @GetMapping("/validar/terminales")
    @PreAuthorize("hasAnyRole('COOPERATIVA', 'ADMIN', 'OFICINISTA')")
    public ResponseEntity<ValidacionTerminalResponse> validarTerminales(
            @RequestParam Long terminalOrigenId,
            @RequestParam Long terminalDestinoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaSalida,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaLlegada) {
        
        return ResponseEntity.ok(asignacionService.validarAsignacionTerminal(
                terminalOrigenId, terminalDestinoId, fecha, horaSalida, horaLlegada));
    }

    /**
     * Asignar una frecuencia (valida y registra)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('COOPERATIVA', 'ADMIN')")
    public ResponseEntity<AsignacionResponse> asignarFrecuencia(@RequestBody AsignacionRequest request) {
        return ResponseEntity.ok(asignacionService.asignarFrecuencia(request));
    }

    /**
     * Obtener resumen de horas semanales de un chofer
     */
    @GetMapping("/chofer/{choferId}/resumen-horas")
    @PreAuthorize("hasAnyRole('COOPERATIVA', 'ADMIN', 'OFICINISTA', 'CHOFER')")
    public ResponseEntity<ResumenHorasChoferResponse> obtenerResumenHoras(
            @PathVariable Long choferId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        
        if (fecha == null) fecha = LocalDate.now();
        return ResponseEntity.ok(asignacionService.obtenerResumenHorasSemana(choferId, fecha));
    }
}
