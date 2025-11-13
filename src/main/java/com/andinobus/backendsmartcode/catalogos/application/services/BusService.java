package com.andinobus.backendsmartcode.catalogos.application.services;

import com.andinobus.backendsmartcode.catalogos.api.dto.BusCreateRequest;
import com.andinobus.backendsmartcode.catalogos.api.dto.BusResponse;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.CooperativaRepository;
import com.andinobus.backendsmartcode.common.errors.NotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("dev")
public class BusService {

    private final BusRepository busRepository;
    private final CooperativaRepository cooperativaRepository;

    public BusService(BusRepository busRepository, CooperativaRepository cooperativaRepository) {
        this.busRepository = busRepository;
        this.cooperativaRepository = cooperativaRepository;
    }

    @Transactional
    public BusResponse create(Long cooperativaId, BusCreateRequest req) {
        Cooperativa coop = cooperativaRepository.findById(cooperativaId)
                .orElseThrow(() -> new NotFoundException("Cooperativa no encontrada"));
        Bus bus = Bus.builder()
                .cooperativa(coop)
                .numeroInterno(req.getNumeroInterno())
                .placa(req.getPlaca())
                .chasisMarca(req.getChasisMarca())
                .carroceriaMarca(req.getCarroceriaMarca())
                .fotoUrl(req.getFotoUrl())
                .activo(req.getActivo() == null ? true : req.getActivo())
                .build();
        bus = busRepository.save(bus);
        return toResponse(bus);
    }

    @Transactional(readOnly = true)
    public Page<BusResponse> listByCooperativa(Long cooperativaId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Bus> data = busRepository.findByCooperativa_IdAndActivoTrue(cooperativaId, pageable);
        return data.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public BusResponse get(Long id) {
        Bus bus = busRepository.findById(id).orElseThrow(() -> new NotFoundException("Bus no encontrado"));
        return toResponse(bus);
    }

    private BusResponse toResponse(Bus b) {
        return BusResponse.builder()
                .id(b.getId())
                .cooperativaId(b.getCooperativa() != null ? b.getCooperativa().getId() : null)
                .numeroInterno(b.getNumeroInterno())
                .placa(b.getPlaca())
                .chasisMarca(b.getChasisMarca())
                .carroceriaMarca(b.getCarroceriaMarca())
                .fotoUrl(b.getFotoUrl())
                .activo(b.getActivo())
                .build();
    }
}
