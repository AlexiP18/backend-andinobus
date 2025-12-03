package com.andinobus.backendsmartcode.catalogos.api.controllers;

import com.andinobus.backendsmartcode.catalogos.api.dto.UsuarioTerminalDtos.*;
import com.andinobus.backendsmartcode.catalogos.application.services.UsuarioTerminalService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Profile("dev")
@RestController
@RequiredArgsConstructor
public class UsuarioTerminalController {

    private final UsuarioTerminalService service;

    // ==================== ENDPOINTS POR USUARIO ====================

    /**
     * Obtiene todos los terminales asignados a un usuario (oficinista)
     */
    @GetMapping("/api/usuarios/{usuarioId}/terminales")
    public ResponseEntity<List<TerminalAsignadoUsuarioResponse>> getTerminalesByUsuario(
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.getTerminalesByUsuario(usuarioId));
    }

    /**
     * Asigna un terminal a un usuario
     */
    @PostMapping("/api/usuarios/{usuarioId}/terminales")
    public ResponseEntity<UsuarioTerminalResponse> asignarTerminal(
            @PathVariable Long usuarioId,
            @RequestBody AsignarTerminalUsuarioRequest request) {
        return ResponseEntity.ok(service.asignarTerminal(usuarioId, request));
    }

    /**
     * Asigna múltiples terminales a un usuario
     */
    @PostMapping("/api/usuarios/{usuarioId}/terminales/batch")
    public ResponseEntity<List<UsuarioTerminalResponse>> asignarTerminales(
            @PathVariable Long usuarioId,
            @RequestBody AsignarTerminalesUsuarioRequest request) {
        return ResponseEntity.ok(service.asignarTerminales(usuarioId, request));
    }

    /**
     * Sincroniza todos los terminales de un usuario (reemplaza los existentes)
     */
    @PutMapping("/api/usuarios/{usuarioId}/terminales/sync")
    public ResponseEntity<List<TerminalAsignadoUsuarioResponse>> sincronizarTerminales(
            @PathVariable Long usuarioId,
            @RequestBody AsignarTerminalesUsuarioRequest request) {
        return ResponseEntity.ok(service.sincronizarTerminales(usuarioId, request));
    }

    /**
     * Actualiza la asignación de un terminal a un usuario
     */
    @PutMapping("/api/usuarios/{usuarioId}/terminales/{terminalId}")
    public ResponseEntity<UsuarioTerminalResponse> updateAsignacion(
            @PathVariable Long usuarioId,
            @PathVariable Long terminalId,
            @RequestBody UpdateUsuarioTerminalRequest request) {
        return ResponseEntity.ok(service.updateAsignacion(usuarioId, terminalId, request));
    }

    /**
     * Desasigna un terminal de un usuario
     */
    @DeleteMapping("/api/usuarios/{usuarioId}/terminales/{terminalId}")
    public ResponseEntity<Void> desasignarTerminal(
            @PathVariable Long usuarioId,
            @PathVariable Long terminalId) {
        service.desasignarTerminal(usuarioId, terminalId);
        return ResponseEntity.noContent().build();
    }

    // ==================== ENDPOINTS POR TERMINAL ====================

    /**
     * Obtiene todos los oficinistas que trabajan en un terminal
     */
    @GetMapping("/api/terminales/{terminalId}/oficinistas")
    public ResponseEntity<List<OficinistaPorTerminalResponse>> getOficinistasByTerminal(
            @PathVariable Long terminalId) {
        return ResponseEntity.ok(service.getOficinistasByTerminal(terminalId));
    }

    // ==================== ENDPOINTS POR COOPERATIVA ====================

    /**
     * Obtiene todos los oficinistas de una cooperativa con sus terminales
     */
    @GetMapping("/api/cooperativas/{cooperativaId}/oficinistas")
    public ResponseEntity<List<UsuarioTerminalResponse>> getOficinistasByCooperativa(
            @PathVariable Long cooperativaId) {
        return ResponseEntity.ok(service.getOficinistasByCooperativa(cooperativaId));
    }
}
