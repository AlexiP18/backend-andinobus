package com.andinobus.backendsmartcode.catalogos.application.services;

import com.andinobus.backendsmartcode.catalogos.api.dto.CooperativaCreateRequest;
import com.andinobus.backendsmartcode.catalogos.api.dto.CooperativaResponse;
import com.andinobus.backendsmartcode.catalogos.api.dto.CooperativaUpdateRequest;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.CooperativaRepository;
import com.andinobus.backendsmartcode.common.errors.NotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CooperativaService {

    private final CooperativaRepository repository;

    public CooperativaService(CooperativaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CooperativaResponse create(CooperativaCreateRequest req) {
        Cooperativa c = Cooperativa.builder()
                .nombre(req.getNombre())
                .ruc(req.getRuc())
                .logoUrl(req.getLogoUrl())
                .activo(req.getActivo() == null ? true : req.getActivo())
                .build();
        c = repository.save(c);
        return toResponse(c);
    }

    @Transactional(readOnly = true)
    public Page<CooperativaResponse> list(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Cooperativa> data;
        if (search == null || search.isBlank()) {
            data = repository.findByActivoTrue(pageable);
        } else {
            data = repository.findByActivoTrueAndNombreContainingIgnoreCaseOrActivoTrueAndRucContainingIgnoreCase(search, search, pageable);
        }
        return data.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CooperativaResponse get(Long id) {
        Cooperativa c = repository.findById(id).orElseThrow(() -> new NotFoundException("Cooperativa no encontrada"));
        return toResponse(c);
    }

    @Transactional
    public CooperativaResponse update(Long id, CooperativaUpdateRequest req) {
        Cooperativa c = repository.findById(id).orElseThrow(() -> new NotFoundException("Cooperativa no encontrada"));
        if (req.getNombre() != null) c.setNombre(req.getNombre());
        if (req.getRuc() != null) c.setRuc(req.getRuc());
        if (req.getLogoUrl() != null) c.setLogoUrl(req.getLogoUrl());
        if (req.getActivo() != null) c.setActivo(req.getActivo());
        c = repository.save(c);
        return toResponse(c);
    }

    @Transactional
    public void deleteLogical(Long id) {
        Cooperativa c = repository.findById(id).orElseThrow(() -> new NotFoundException("Cooperativa no encontrada"));
        c.setActivo(false);
        repository.save(c);
    }

    private CooperativaResponse toResponse(Cooperativa c) {
        return CooperativaResponse.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .ruc(c.getRuc())
                .logoUrl(c.getLogoUrl())
                .activo(c.getActivo())
                .build();
    }
}
