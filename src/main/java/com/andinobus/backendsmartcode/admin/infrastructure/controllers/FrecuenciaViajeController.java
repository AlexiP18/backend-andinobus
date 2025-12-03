package com.andinobus.backendsmartcode.admin.infrastructure.controllers;

import com.andinobus.backendsmartcode.admin.application.dtos.FrecuenciaDtos.*;
import com.andinobus.backendsmartcode.admin.application.services.FrecuenciaViajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Profile("dev")
@RestController
@RequestMapping("/api/admin/frecuencias")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FrecuenciaViajeController {

    private final FrecuenciaViajeService frecuenciaService;

    @GetMapping("/bus/{busId}")
    public ResponseEntity<List<FrecuenciaViajeResponse>> getFrecuenciasByBus(@PathVariable Long busId) {
        return ResponseEntity.ok(frecuenciaService.getAllByBus(busId));
    }

    @GetMapping("/cooperativa/{cooperativaId}")
    public ResponseEntity<List<FrecuenciaViajeResponse>> getFrecuenciasByCooperativa(@PathVariable Long cooperativaId) {
        return ResponseEntity.ok(frecuenciaService.getAllByCooperativa(cooperativaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FrecuenciaViajeResponse> getFrecuenciaById(@PathVariable Long id) {
        return ResponseEntity.ok(frecuenciaService.getById(id));
    }

    @PostMapping
    public ResponseEntity<FrecuenciaViajeResponse> createFrecuencia(@RequestBody CreateFrecuenciaRequest request) {
        FrecuenciaViajeResponse response = frecuenciaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FrecuenciaViajeResponse> updateFrecuencia(
            @PathVariable Long id,
            @RequestBody UpdateFrecuenciaRequest request) {
        return ResponseEntity.ok(frecuenciaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFrecuencia(@PathVariable Long id) {
        frecuenciaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina TODAS las frecuencias de una cooperativa
     */
    @DeleteMapping("/cooperativa/{cooperativaId}/all")
    public ResponseEntity<java.util.Map<String, Object>> deleteAllFrecuencias(@PathVariable Long cooperativaId) {
        int count = frecuenciaService.deleteAllByCooperativa(cooperativaId);
        return ResponseEntity.ok(java.util.Map.of(
            "message", "Frecuencias eliminadas correctamente",
            "count", count
        ));
    }
}
