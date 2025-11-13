package com.andinobus.backendsmartcode.embarque.api.controllers;

import com.andinobus.backendsmartcode.embarque.api.dto.EmbarqueDtos;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class EmbarqueController {

    private static final Set<String> USADOS = ConcurrentHashMap.newKeySet();

    @PostMapping({"/embarque/scan", "/embarcque/scan"})
    @ResponseStatus(HttpStatus.OK)
    public EmbarqueDtos.ScanResponse scan(@RequestBody EmbarqueDtos.ScanRequest req) {
        String codigo = req.getCodigo();
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("codigo es requerido");
        }
        boolean valido = codigo.length() >= 6; // regla mock
        String estado = "emitido";
        String message = "OK";
        if (!valido) {
            estado = "invalido";
            message = "Código no válido";
        } else if (USADOS.contains(codigo)) {
            estado = "usado";
            message = "Boleto ya fue usado";
        } else {
            // marcar como usado en esta llamada (simula scan de acceso)
            USADOS.add(codigo);
            estado = "usado";
            message = "Acceso registrado";
        }
        return EmbarqueDtos.ScanResponse.builder()
                .codigo(codigo)
                .valido(valido)
                .estado(estado)
                .message(message)
                .build();
    }
}
