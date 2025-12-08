package com.andinobus.backendsmartcode.ventas.api.controllers;

import com.andinobus.backendsmartcode.ventas.api.dto.VentasDtos;
import com.andinobus.backendsmartcode.ventas.application.services.BoletoService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boletos")
@RequiredArgsConstructor
public class BoletosController {

    private final BoletoService boletoService;

    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<VentasDtos.BoletoResponse> generarBoleto(
            @PathVariable Long reservaId,
            @RequestParam(required = false) String clienteEmail,
            Authentication authentication) {
        
        String email = clienteEmail != null ? clienteEmail : (authentication != null ? authentication.getName() : null);
        VentasDtos.BoletoResponse response = boletoService.generarBoleto(reservaId, email);
        return ResponseEntity.ok(response);
    }
}
