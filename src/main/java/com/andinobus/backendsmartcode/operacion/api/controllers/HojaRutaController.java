package com.andinobus.backendsmartcode.operacion.api.controllers;

import com.andinobus.backendsmartcode.operacion.api.dto.HojaRutaDtos;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class HojaRutaController {

    @PostMapping("/hojas-ruta/generar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public HojaRutaDtos.GenerarResponse generar(@Valid @RequestBody HojaRutaDtos.GenerarRequest req) {
        // Stub: respuesta aceptada, sin procesamiento real
        return HojaRutaDtos.GenerarResponse.builder()
                .fecha(req.getFecha())
                .cooperativaId(req.getCooperativaId())
                .modo(req.getModo())
                .viajesGenerados(0)
                .status("ACCEPTED")
                .message("Generación encolada (stub)")
                .build();
    }
}
