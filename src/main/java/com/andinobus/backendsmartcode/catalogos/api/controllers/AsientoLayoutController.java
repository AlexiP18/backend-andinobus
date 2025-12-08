package com.andinobus.backendsmartcode.catalogos.api.controllers;

import com.andinobus.backendsmartcode.catalogos.api.dto.AsientoLayoutDtos.*;
import com.andinobus.backendsmartcode.catalogos.application.services.AsientoLayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de layout de asientos de buses
 */
@RestController
@RequestMapping("/api/buses/{busId}/asientos")
@RequiredArgsConstructor
public class AsientoLayoutController {

    private final AsientoLayoutService asientoLayoutService;

    /**
     * Obtiene el layout completo de asientos de un bus
     * 
     * GET /api/buses/{busId}/asientos
     */
    @GetMapping
    public ResponseEntity<BusLayoutResponse> getLayout(@PathVariable Long busId) {
        BusLayoutResponse layout = asientoLayoutService.getLayout(busId);
        return ResponseEntity.ok(layout);
    }

    /**
     * Genera el layout automático en grid para un bus
     * 
     * POST /api/buses/{busId}/asientos/generate
     * 
     * Body: {
     *   "filas": 10,
     *   "columnas": 4,
     *   "sobrescribir": false
     * }
     */
    @PostMapping("/generate")
    public ResponseEntity<AsientoOperationResponse> generateLayout(
            @PathVariable Long busId,
            @RequestBody GenerateLayoutRequest request
    ) {
        AsientoOperationResponse response = asientoLayoutService.generateLayout(busId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza un asiento individual
     * 
     * PUT /api/buses/{busId}/asientos/{asientoId}
     * 
     * Body: {
     *   "tipoAsiento": "VIP",
     *   "habilitado": true
     * }
     */
    @PutMapping("/{asientoId}")
    public ResponseEntity<AsientoResponse> updateAsiento(
            @PathVariable Long busId,
            @PathVariable Long asientoId,
            @RequestBody UpdateAsientoRequest request
    ) {
        AsientoResponse response = asientoLayoutService.updateAsiento(busId, asientoId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza múltiples asientos a la vez
     * 
     * PUT /api/buses/{busId}/asientos/bulk
     * 
     * Body: {
     *   "asientos": [
     *     { "id": 1, "tipoAsiento": "VIP", "habilitado": true },
     *     { "numeroAsiento": 5, "tipoAsiento": "ACONDICIONADO", "habilitado": true }
     *   ]
     * }
     */
    @PutMapping("/bulk")
    public ResponseEntity<AsientoOperationResponse> bulkUpdateAsientos(
            @PathVariable Long busId,
            @RequestBody BulkUpdateAsientosRequest request
    ) {
        AsientoOperationResponse response = asientoLayoutService.bulkUpdateAsientos(busId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina el layout completo de un bus
     * 
     * DELETE /api/buses/{busId}/asientos
     */
    @DeleteMapping
    public ResponseEntity<AsientoOperationResponse> deleteLayout(@PathVariable Long busId) {
        AsientoOperationResponse response = asientoLayoutService.deleteLayout(busId);
        return ResponseEntity.ok(response);
    }
}
