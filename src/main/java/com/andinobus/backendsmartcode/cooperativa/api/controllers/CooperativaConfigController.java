package com.andinobus.backendsmartcode.cooperativa.api.controllers;

import com.andinobus.backendsmartcode.cooperativa.api.dto.CooperativaConfigDtos.*;
import com.andinobus.backendsmartcode.cooperativa.application.services.CooperativaConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cooperativas/{cooperativaId}/configuracion")
@RequiredArgsConstructor
public class CooperativaConfigController {

    private final CooperativaConfigService configService;

    /**
     * Obtener la configuración de una cooperativa
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COOPERATIVA')")
    public ResponseEntity<ConfiguracionResponse> getConfiguracion(
            @PathVariable Long cooperativaId) {
        return ResponseEntity.ok(configService.getConfiguracion(cooperativaId));
    }

    /**
     * Actualizar la configuración de una cooperativa
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COOPERATIVA')")
    public ResponseEntity<ConfiguracionResponse> updateConfiguracion(
            @PathVariable Long cooperativaId,
            @RequestBody UpdateConfiguracionRequest request) {
        return ResponseEntity.ok(configService.updateConfiguracion(cooperativaId, request));
    }

    /**
     * Subir logo de la cooperativa (Base64)
     */
    @PostMapping("/logo")
    @PreAuthorize("hasAnyRole('ADMIN', 'COOPERATIVA')")
    public ResponseEntity<ConfiguracionResponse> uploadLogo(
            @PathVariable Long cooperativaId,
            @RequestBody UpdateLogoRequest request) {
        return ResponseEntity.ok(configService.uploadLogo(cooperativaId, request));
    }

    /**
     * Eliminar logo de la cooperativa
     */
    @DeleteMapping("/logo")
    @PreAuthorize("hasAnyRole('ADMIN', 'COOPERATIVA')")
    public ResponseEntity<ConfiguracionResponse> deleteLogo(
            @PathVariable Long cooperativaId) {
        return ResponseEntity.ok(configService.deleteLogo(cooperativaId));
    }
}
