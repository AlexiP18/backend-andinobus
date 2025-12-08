package com.andinobus.backendsmartcode.ventas.api.controllers;

import com.andinobus.backendsmartcode.ventas.api.dto.VentaPresencialDtos.*;
import com.andinobus.backendsmartcode.ventas.application.services.VentaPresencialService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para ventas presenciales desde oficinistas
 * Las ventas se realizan basándose en frecuencias de la cooperativa
 */
@RestController
@RequestMapping("/api/ventas-presenciales")
@RequiredArgsConstructor
public class VentaPresencialController {

    private final VentaPresencialService ventaPresencialService;

    /**
     * Crea una venta presencial directa basada en frecuencia
     * POST /api/ventas-presenciales
     */
    @PostMapping
    public ResponseEntity<VentaPresencialResponse> crearVentaPresencial(
            @RequestBody CreateVentaPresencialRequest request
    ) {
        VentaPresencialResponse response = ventaPresencialService.crearVentaPresencialDesdeFrecuencia(request);
        return ResponseEntity.ok(response);
    }
}
