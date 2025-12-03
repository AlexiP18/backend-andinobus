package com.andinobus.backendsmartcode.catalogos.api.controllers;

import com.andinobus.backendsmartcode.catalogos.api.dto.TerminalDtos.*;
import com.andinobus.backendsmartcode.catalogos.application.services.TerminalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terminales")
@RequiredArgsConstructor
public class TerminalController {

    private final TerminalService terminalService;

    /**
     * Listar todos los terminales activos
     * Acceso: Todos los usuarios autenticados
     */
    @GetMapping
    public ResponseEntity<List<TerminalResponse>> listarTodos() {
        return ResponseEntity.ok(terminalService.listarTodos());
    }

    /**
     * Obtener terminal por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TerminalResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(terminalService.obtenerPorId(id));
    }

    /**
     * Listar terminales por provincia
     */
    @GetMapping("/provincia/{provincia}")
    public ResponseEntity<List<TerminalResponse>> listarPorProvincia(@PathVariable String provincia) {
        return ResponseEntity.ok(terminalService.listarPorProvincia(provincia));
    }

    /**
     * Listar terminales por tipología (T1, T2, T3, T4, T5)
     */
    @GetMapping("/tipologia/{tipologia}")
    public ResponseEntity<List<TerminalResponse>> listarPorTipologia(@PathVariable String tipologia) {
        return ResponseEntity.ok(terminalService.listarPorTipologia(tipologia));
    }

    /**
     * Buscar terminales por texto
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<TerminalResponse>> buscar(@RequestParam String q) {
        return ResponseEntity.ok(terminalService.buscar(q));
    }

    /**
     * Listar provincias que tienen terminales
     */
    @GetMapping("/provincias")
    public ResponseEntity<List<String>> listarProvincias() {
        return ResponseEntity.ok(terminalService.listarProvincias());
    }

    /**
     * Obtener estadísticas de terminales
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<TerminalStatsResponse> obtenerEstadisticas() {
        return ResponseEntity.ok(terminalService.obtenerEstadisticas());
    }

    /**
     * Crear nuevo terminal
     * Acceso: Solo SuperAdmin
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TerminalResponse> crear(@RequestBody TerminalCreateRequest request) {
        return ResponseEntity.ok(terminalService.crear(request));
    }

    /**
     * Actualizar terminal
     * Acceso: Solo SuperAdmin
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TerminalResponse> actualizar(
            @PathVariable Long id,
            @RequestBody TerminalUpdateRequest request) {
        return ResponseEntity.ok(terminalService.actualizar(id, request));
    }

    /**
     * Desactivar terminal
     * Acceso: Solo SuperAdmin
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        terminalService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
