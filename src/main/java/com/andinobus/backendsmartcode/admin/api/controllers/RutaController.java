package com.andinobus.backendsmartcode.admin.api.controllers;

import com.andinobus.backendsmartcode.admin.api.dto.RutaDtos.*;
import com.andinobus.backendsmartcode.admin.application.services.RutaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Profile("dev")
@RestController
@RequestMapping("/api/admin/rutas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RutaController {

    private final RutaService rutaService;

    @GetMapping
    public ResponseEntity<List<RutaResponse>> getAllRutas(@RequestParam(required = false) String filter) {
        log.info("GET /api/admin/rutas - filter: {}", filter);
        
        List<RutaResponse> rutas;
        if ("activas".equals(filter)) {
            rutas = rutaService.getRutasActivas();
        } else if ("aprobadas".equals(filter)) {
            rutas = rutaService.getRutasAprobadas();
        } else {
            rutas = rutaService.getAllRutas();
        }
        
        return ResponseEntity.ok(rutas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RutaResponse> getRutaById(@PathVariable Long id) {
        log.info("GET /api/admin/rutas/{}", id);
        RutaResponse ruta = rutaService.getRutaById(id);
        return ResponseEntity.ok(ruta);
    }

    @PostMapping
    public ResponseEntity<RutaResponse> createRuta(@RequestBody CreateRutaRequest request) {
        log.info("POST /api/admin/rutas - nombre: {}", request.getNombre());
        RutaResponse ruta = rutaService.createRuta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ruta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RutaResponse> updateRuta(@PathVariable Long id, @RequestBody UpdateRutaRequest request) {
        log.info("PUT /api/admin/rutas/{}", id);
        RutaResponse ruta = rutaService.updateRuta(id, request);
        return ResponseEntity.ok(ruta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRuta(@PathVariable Long id) {
        log.info("DELETE /api/admin/rutas/{}", id);
        rutaService.deleteRuta(id);
        return ResponseEntity.noContent().build();
    }
}
