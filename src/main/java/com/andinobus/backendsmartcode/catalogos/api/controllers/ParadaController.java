package com.andinobus.backendsmartcode.catalogos.api.controllers;

import com.andinobus.backendsmartcode.catalogos.api.dto.ParadaCreateRequest;
import com.andinobus.backendsmartcode.catalogos.api.dto.ParadaResponse;
import com.andinobus.backendsmartcode.catalogos.api.dto.ParadaUpdateRequest;
import com.andinobus.backendsmartcode.catalogos.application.services.ParadaService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ParadaController {

    private final ParadaService service;

    public ParadaController(ParadaService service) {
        this.service = service;
    }

    @PostMapping("/frecuencias/{frecuenciaId}/paradas")
    @ResponseStatus(HttpStatus.CREATED)
    public ParadaResponse create(@PathVariable Long frecuenciaId, @Valid @RequestBody ParadaCreateRequest request) {
        return service.create(frecuenciaId, request);
    }

    @GetMapping("/frecuencias/{frecuenciaId}/paradas")
    public List<ParadaResponse> list(@PathVariable Long frecuenciaId) {
        return service.listByFrecuencia(frecuenciaId);
    }

    @GetMapping("/paradas/{id}")
    public ParadaResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/paradas/{id}")
    public ParadaResponse update(@PathVariable Long id, @Valid @RequestBody ParadaUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/paradas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
