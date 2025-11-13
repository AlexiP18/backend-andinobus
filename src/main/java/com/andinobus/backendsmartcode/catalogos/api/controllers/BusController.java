package com.andinobus.backendsmartcode.catalogos.api.controllers;

import com.andinobus.backendsmartcode.catalogos.api.dto.BusCreateRequest;
import com.andinobus.backendsmartcode.catalogos.api.dto.BusResponse;
import com.andinobus.backendsmartcode.catalogos.application.services.BusService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Profile("dev")
@RestController
public class BusController {

    private final BusService service;

    public BusController(BusService service) {
        this.service = service;
    }

    @PostMapping("/cooperativas/{cooperativaId}/buses")
    @ResponseStatus(HttpStatus.CREATED)
    public BusResponse create(@PathVariable Long cooperativaId, @Valid @RequestBody BusCreateRequest request) {
        return service.create(cooperativaId, request);
    }

    @GetMapping("/cooperativas/{cooperativaId}/buses")
    public Page<BusResponse> listByCooperativa(@PathVariable Long cooperativaId,
                                               @RequestParam(value = "page", defaultValue = "0") int page,
                                               @RequestParam(value = "size", defaultValue = "20") int size) {
        return service.listByCooperativa(cooperativaId, page, size);
    }

    @GetMapping("/buses/{id}")
    public BusResponse get(@PathVariable Long id) {
        return service.get(id);
    }
}
