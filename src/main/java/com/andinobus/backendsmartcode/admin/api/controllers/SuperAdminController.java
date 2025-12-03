package com.andinobus.backendsmartcode.admin.api.controllers;

import com.andinobus.backendsmartcode.admin.api.dto.SuperAdminDtos;
import com.andinobus.backendsmartcode.admin.application.services.SuperAdminStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Profile("dev")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SuperAdminController {

    private final SuperAdminStatsService superAdminStatsService;

    /**
     * Obtener estadísticas globales del sistema para el Super Admin
     */
    @GetMapping("/stats")
    public ResponseEntity<SuperAdminDtos.SuperAdminStatsResponse> getStats() {
        log.info("GET /api/admin/stats - Obteniendo estadísticas globales");
        SuperAdminDtos.SuperAdminStatsResponse stats = superAdminStatsService.getStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Obtener lista de todas las cooperativas con información resumida
     */
    @GetMapping("/cooperativas")
    public ResponseEntity<List<SuperAdminDtos.CooperativaInfo>> getAllCooperativas() {
        log.info("GET /api/admin/cooperativas - Obteniendo lista de cooperativas");
        List<SuperAdminDtos.CooperativaInfo> cooperativas = superAdminStatsService.getAllCooperativas();
        return ResponseEntity.ok(cooperativas);
    }

    /**
     * Obtener detalle completo de una cooperativa específica
     */
    @GetMapping("/cooperativas/{id}")
    public ResponseEntity<SuperAdminDtos.CooperativaDetalleResponse> getCooperativaDetalle(@PathVariable Long id) {
        log.info("GET /api/admin/cooperativas/{} - Obteniendo detalle de cooperativa", id);
        SuperAdminDtos.CooperativaDetalleResponse detalle = superAdminStatsService.getCooperativaDetalle(id);
        return ResponseEntity.ok(detalle);
    }

    /**
     * Activar o desactivar una cooperativa
     */
    @PatchMapping("/cooperativas/{id}/toggle-estado")
    public ResponseEntity<Void> toggleCooperativaEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        log.info("PATCH /api/admin/cooperativas/{}/toggle-estado?activo={}", id, activo);
        superAdminStatsService.toggleCooperativaEstado(id, activo);
        return ResponseEntity.ok().build();
    }

    /**
     * Obtener lista de todos los clientes
     */
    @GetMapping("/clientes")
    public ResponseEntity<List<SuperAdminDtos.ClienteInfo>> getAllClientes() {
        log.info("GET /api/admin/clientes - Obteniendo lista de clientes");
        List<SuperAdminDtos.ClienteInfo> clientes = superAdminStatsService.getAllClientes();
        return ResponseEntity.ok(clientes);
    }

    /**
     * Activar o desactivar un cliente
     */
    @PatchMapping("/clientes/{id}/toggle-estado")
    public ResponseEntity<Void> toggleClienteEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        log.info("PATCH /api/admin/clientes/{}/toggle-estado?activo={}", id, activo);
        superAdminStatsService.toggleClienteEstado(id, activo);
        return ResponseEntity.ok().build();
    }
}
