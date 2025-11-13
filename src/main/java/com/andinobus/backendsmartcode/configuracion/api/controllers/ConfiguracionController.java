package com.andinobus.backendsmartcode.configuracion.api.controllers;

import com.andinobus.backendsmartcode.configuracion.api.dto.ConfiguracionDtos;
import org.springframework.web.bind.annotation.*;

@RestController
public class ConfiguracionController {

    private ConfiguracionDtos.ConfiguracionResponse current = ConfiguracionDtos.ConfiguracionResponse.builder()
            .logoUrl(null)
            .colorPrimario("#0052CC")
            .colorSecundario("#36B37E")
            .facebook(null)
            .instagram(null)
            .soporteEmail("soporte@example.com")
            .soporteTelefono("+593-000-000-000")
            .build();

    @GetMapping("/configuracion")
    public ConfiguracionDtos.ConfiguracionResponse get() {
        return current;
    }

    @PutMapping("/configuracion")
    public ConfiguracionDtos.ConfiguracionResponse put(@RequestBody ConfiguracionDtos.ConfiguracionRequest req) {
        current = ConfiguracionDtos.ConfiguracionResponse.builder()
                .logoUrl(req.getLogoUrl())
                .colorPrimario(req.getColorPrimario())
                .colorSecundario(req.getColorSecundario())
                .facebook(req.getFacebook())
                .instagram(req.getInstagram())
                .soporteEmail(req.getSoporteEmail())
                .soporteTelefono(req.getSoporteTelefono())
                .build();
        return current;
    }
}
