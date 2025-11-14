package com.andinobus.backendsmartcode.embarque.api.controllers;

import com.andinobus.backendsmartcode.embarque.api.dto.EmbarqueDtos;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class EmbarqueController {

    private static final Set<String> USADOS = ConcurrentHashMap.newKeySet();
    private final JdbcTemplate jdbcTemplate; // puede ser null si no hay DataSource (perfil por defecto)

    public EmbarqueController(ObjectProvider<JdbcTemplate> jdbcProvider) {
        this.jdbcTemplate = jdbcProvider != null ? jdbcProvider.getIfAvailable() : null;
    }

    @PostMapping({"/embarque/scan", "/embarcque/scan"})
    @ResponseStatus(HttpStatus.OK)
    public EmbarqueDtos.ScanResponse scan(@RequestBody EmbarqueDtos.ScanRequest req) {
        String codigo = req.getCodigo();
        if (codigo == null || codigo.isBlank()) {
            logScan(codigo, "invalido", "codigo es requerido");
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
        logScan(codigo, (valido ? estado : "invalido"), message);
        return EmbarqueDtos.ScanResponse.builder()
                .codigo(codigo)
                .valido(valido)
                .estado(estado)
                .message(message)
                .build();
    }

    private void logScan(String codigo, String resultado, String message) {
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.update(
                        "INSERT INTO embarque_scan_log (codigo, resultado, message) VALUES (?,?,?)",
                        codigo, resultado, message
                );
            } catch (Exception ignore) {
                // no romper flujo si la tabla no existe o hay error de conexión
            }
        }
    }
}
