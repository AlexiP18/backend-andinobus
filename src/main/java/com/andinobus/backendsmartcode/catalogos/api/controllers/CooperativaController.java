package com.andinobus.backendsmartcode.catalogos.api.controllers;

import com.andinobus.backendsmartcode.catalogos.api.dto.CooperativaCreateRequest;
import com.andinobus.backendsmartcode.catalogos.api.dto.CooperativaResponse;
import com.andinobus.backendsmartcode.catalogos.api.dto.CooperativaUpdateRequest;
import com.andinobus.backendsmartcode.catalogos.application.services.CooperativaService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Profile("dev")
@RestController
@RequestMapping("/cooperativas")
public class CooperativaController {

    private final CooperativaService service;

    public CooperativaController(CooperativaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CooperativaResponse create(@Valid @RequestBody CooperativaCreateRequest request) {
        return service.create(request);
    }

    @GetMapping
    public Page<CooperativaResponse> list(@RequestParam(value = "search", required = false) String search,
                                          @RequestParam(value = "page", defaultValue = "0") int page,
                                          @RequestParam(value = "size", defaultValue = "20") int size) {
        return service.list(search, page, size);
    }

    @GetMapping("/{id}")
    public CooperativaResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public CooperativaResponse update(@PathVariable Long id, @Valid @RequestBody CooperativaUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteLogical(id);
    }
}
