package com.andinobus.backendsmartcode.admin.api.controllers;

import com.andinobus.backendsmartcode.admin.api.dto.ConfiguracionGlobalDtos.*;
import com.andinobus.backendsmartcode.admin.application.services.ConfiguracionGlobalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/configuracion")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConfiguracionGlobalController {

    private final ConfiguracionGlobalService configuracionService;

    @GetMapping
    public ResponseEntity<ConfiguracionGlobalResponse> getConfiguracion() {
        log.info("GET /api/admin/configuracion");
        ConfiguracionGlobalResponse config = configuracionService.getConfiguracion();
        return ResponseEntity.ok(config);
    }

    @PutMapping
    public ResponseEntity<ConfiguracionGlobalResponse> updateConfiguracion(
            @RequestBody UpdateConfiguracionRequest request,
            @RequestHeader(value = "User-Email", defaultValue = "admin") String userEmail) {
        log.info("PUT /api/admin/configuracion - updatedBy: {}", userEmail);
        ConfiguracionGlobalResponse config = configuracionService.updateConfiguracion(request, userEmail);
        return ResponseEntity.ok(config);
    }
}
