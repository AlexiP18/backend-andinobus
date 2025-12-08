package com.andinobus.backendsmartcode.ubicacion.api.controllers;

import com.andinobus.backendsmartcode.ubicacion.application.services.UbicacionService;
import com.andinobus.backendsmartcode.ubicacion.domain.entities.Provincia;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ubicacion")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UbicacionController {

    private final UbicacionService ubicacionService;

    @GetMapping("/provincias")
    public ResponseEntity<List<Provincia>> getProvincias() {
        log.info("GET /api/ubicacion/provincias");
        List<Provincia> provincias = ubicacionService.getTodasLasProvincias();
        return ResponseEntity.ok(provincias);
    }

    @GetMapping("/provincias/{nombre}")
    public ResponseEntity<Provincia> getProvinciaPorNombre(@PathVariable String nombre) {
        log.info("GET /api/ubicacion/provincias/{}", nombre);
        Provincia provincia = ubicacionService.getProvinciaPorNombre(nombre);
        if (provincia == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(provincia);
    }
}
