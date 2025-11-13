package com.andinobus.backendsmartcode.catalogos.api.controllers;

import com.andinobus.backendsmartcode.catalogos.api.dto.FrecuenciaCreateRequest;
import com.andinobus.backendsmartcode.catalogos.api.dto.FrecuenciaResponse;
import com.andinobus.backendsmartcode.catalogos.api.dto.FrecuenciaUpdateRequest;
import com.andinobus.backendsmartcode.catalogos.application.services.FrecuenciaService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Profile("dev")
@RestController
public class FrecuenciaController {

    private final FrecuenciaService service;

    public FrecuenciaController(FrecuenciaService service) {
        this.service = service;
    }

    @PostMapping("/cooperativas/{cooperativaId}/frecuencias")
    @ResponseStatus(HttpStatus.CREATED)
    public FrecuenciaResponse create(@PathVariable Long cooperativaId, @Valid @RequestBody FrecuenciaCreateRequest request) {
        return service.create(cooperativaId, request);
    }

    @GetMapping("/cooperativas/{cooperativaId}/frecuencias")
    public Page<FrecuenciaResponse> listByCooperativa(@PathVariable Long cooperativaId,
                                                      @RequestParam(value = "search", required = false) String search,
                                                      @RequestParam(value = "page", defaultValue = "0") int page,
                                                      @RequestParam(value = "size", defaultValue = "20") int size) {
        return service.listByCooperativa(cooperativaId, search, page, size);
    }

    @GetMapping("/frecuencias/{id}")
    public FrecuenciaResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/frecuencias/{id}")
    public FrecuenciaResponse update(@PathVariable Long id, @Valid @RequestBody FrecuenciaUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/frecuencias/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteLogical(id);
    }
}
