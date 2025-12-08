package com.andinobus.backendsmartcode.catalogos.api.controllers;

import com.andinobus.backendsmartcode.catalogos.api.dto.CooperativaTerminalDtos.*;
import com.andinobus.backendsmartcode.catalogos.application.services.CooperativaTerminalService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cooperativas/{cooperativaId}/terminales")
@RequiredArgsConstructor
public class CooperativaTerminalController {

    private final CooperativaTerminalService service;

    /**
     * Obtiene todos los terminales asignados a una cooperativa
     */
    @GetMapping
    public ResponseEntity<List<TerminalAsignadoResponse>> getTerminales(
            @PathVariable Long cooperativaId) {
        return ResponseEntity.ok(service.getTerminalesByCooperativa(cooperativaId));
    }

    /**
     * Asigna un terminal a la cooperativa
     */
    @PostMapping
    public ResponseEntity<CooperativaTerminalResponse> asignarTerminal(
            @PathVariable Long cooperativaId,
            @RequestBody AsignarTerminalRequest request) {
        return ResponseEntity.ok(service.asignarTerminal(cooperativaId, request));
    }

    /**
     * Asigna múltiples terminales a la cooperativa
     */
    @PostMapping("/batch")
    public ResponseEntity<List<CooperativaTerminalResponse>> asignarTerminales(
            @PathVariable Long cooperativaId,
            @RequestBody AsignarTerminalesRequest request) {
        return ResponseEntity.ok(service.asignarTerminales(cooperativaId, request));
    }

    /**
     * Sincroniza todos los terminales de una cooperativa (reemplaza los existentes)
     */
    @PutMapping("/sync")
    public ResponseEntity<List<TerminalAsignadoResponse>> sincronizarTerminales(
            @PathVariable Long cooperativaId,
            @RequestBody List<Long> terminalIds) {
        return ResponseEntity.ok(service.sincronizarTerminales(cooperativaId, terminalIds));
    }

    /**
     * Actualiza la asignación de un terminal
     */
    @PutMapping("/{terminalId}")
    public ResponseEntity<CooperativaTerminalResponse> updateAsignacion(
            @PathVariable Long cooperativaId,
            @PathVariable Long terminalId,
            @RequestBody UpdateCooperativaTerminalRequest request) {
        return ResponseEntity.ok(service.updateAsignacion(cooperativaId, terminalId, request));
    }

    /**
     * Desasigna un terminal de la cooperativa
     */
    @DeleteMapping("/{terminalId}")
    public ResponseEntity<Void> desasignarTerminal(
            @PathVariable Long cooperativaId,
            @PathVariable Long terminalId) {
        service.desasignarTerminal(cooperativaId, terminalId);
        return ResponseEntity.noContent().build();
    }
}
