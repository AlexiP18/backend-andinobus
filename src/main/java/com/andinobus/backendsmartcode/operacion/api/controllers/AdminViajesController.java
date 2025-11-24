package com.andinobus.backendsmartcode.operacion.api.controllers;

import com.andinobus.backendsmartcode.operacion.application.services.ViajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Profile("dev")
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminViajesController {

    private final ViajeService viajeService;

    /**
     * Endpoint para programar viajes automáticamente
     * POST /admin/viajes/programar?dias=7
     */
    @PostMapping("/viajes/programar")
    public ResponseEntity<Map<String, String>> programarViajes(
            @RequestParam(defaultValue = "7") int dias
    ) {
        try {
            viajeService.programarViajesParaLosSiguientesDias(dias);
            return ResponseEntity.ok(Map.of(
                    "message", "Viajes programados exitosamente para los próximos " + dias + " días",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Error al programar viajes: " + e.getMessage(),
                    "status", "error"
            ));
        }
    }
}
