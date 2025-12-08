package com.andinobus.backendsmartcode.ventas.api.controllers;

import com.andinobus.backendsmartcode.ventas.api.dto.VentasDtos;
import com.andinobus.backendsmartcode.ventas.application.services.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagosController {

    private final PagoService pagoService;

    @PostMapping("/confirmar")
    public ResponseEntity<VentasDtos.PagoResponse> confirmarPago(
            @RequestBody VentasDtos.PagoConfirmacionRequest request,
            @RequestParam(required = false) String clienteEmail,
            Authentication authentication) {
        
        String email = clienteEmail != null ? clienteEmail : (authentication != null ? authentication.getName() : null);
        VentasDtos.PagoResponse response = pagoService.confirmarPago(request, email);
        return ResponseEntity.ok(response);
    }
}
