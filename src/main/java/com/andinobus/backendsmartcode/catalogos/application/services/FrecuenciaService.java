package com.andinobus.backendsmartcode.catalogos.application.services;

import com.andinobus.backendsmartcode.catalogos.api.dto.FrecuenciaCreateRequest;
import com.andinobus.backendsmartcode.catalogos.api.dto.FrecuenciaResponse;
import com.andinobus.backendsmartcode.catalogos.api.dto.FrecuenciaUpdateRequest;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Frecuencia;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.CooperativaRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.FrecuenciaRepository;
import com.andinobus.backendsmartcode.common.errors.NotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class FrecuenciaService {

    private final FrecuenciaRepository frecuenciaRepository;
    private final CooperativaRepository cooperativaRepository;

    public FrecuenciaService(FrecuenciaRepository frecuenciaRepository, CooperativaRepository cooperativaRepository) {
        this.frecuenciaRepository = frecuenciaRepository;
        this.cooperativaRepository = cooperativaRepository;
    }

    @Transactional
    public FrecuenciaResponse create(Long cooperativaId, FrecuenciaCreateRequest req) {
        Cooperativa coop = cooperativaRepository.findById(cooperativaId)
                .orElseThrow(() -> new NotFoundException("Cooperativa no encontrada"));
        LocalTime horaSalida = parseTime(req.getHoraSalida());
        if (horaSalida == null) {
            throw new IllegalArgumentException("Formato inválido de horaSalida. Use HH:mm o equivalente");
        }
        Frecuencia f = Frecuencia.builder()
                .cooperativa(coop)
                .origen(req.getOrigen())
                .destino(req.getDestino())
                .horaSalida(horaSalida)
                .duracionEstimadaMin(req.getDuracionEstimadaMin())
                .diasOperacion(req.getDiasOperacion())
                .activa(req.getActiva() == null ? true : req.getActiva())
                .build();
        f = frecuenciaRepository.save(f);
        return toResponse(f);
    }

    @Transactional(readOnly = true)
    public Page<FrecuenciaResponse> listByCooperativa(Long cooperativaId, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Frecuencia> data;
        if (search == null || search.isBlank()) {
            data = frecuenciaRepository.findByCooperativa_IdAndActivaTrue(cooperativaId, pageable);
        } else {
            data = frecuenciaRepository.findByCooperativa_IdAndActivaTrueAndOrigenContainingIgnoreCaseOrCooperativa_IdAndActivaTrueAndDestinoContainingIgnoreCase(
                    cooperativaId, search, cooperativaId, search, pageable);
        }
        return data.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public FrecuenciaResponse get(Long id) {
        Frecuencia f = frecuenciaRepository.findById(id).orElseThrow(() -> new NotFoundException("Frecuencia no encontrada"));
        return toResponse(f);
    }

    @Transactional
    public FrecuenciaResponse update(Long id, FrecuenciaUpdateRequest req) {
        Frecuencia f = frecuenciaRepository.findById(id).orElseThrow(() -> new NotFoundException("Frecuencia no encontrada"));
        if (req.getOrigen() != null) f.setOrigen(req.getOrigen());
        if (req.getDestino() != null) f.setDestino(req.getDestino());
        if (req.getHoraSalida() != null) {
            LocalTime hs = parseTime(req.getHoraSalida());
            if (hs == null) {
                throw new IllegalArgumentException("Formato inválido de horaSalida. Use HH:mm o equivalente");
            }
            f.setHoraSalida(hs);
        }
        if (req.getDuracionEstimadaMin() != null) f.setDuracionEstimadaMin(req.getDuracionEstimadaMin());
        if (req.getDiasOperacion() != null) f.setDiasOperacion(req.getDiasOperacion());
        if (req.getActiva() != null) f.setActiva(req.getActiva());
        f = frecuenciaRepository.save(f);
        return toResponse(f);
    }

    @Transactional
    public void deleteLogical(Long id) {
        Frecuencia f = frecuenciaRepository.findById(id).orElseThrow(() -> new NotFoundException("Frecuencia no encontrada"));
        f.setActiva(false);
        frecuenciaRepository.save(f);
    }

    private FrecuenciaResponse toResponse(Frecuencia f) {
        return FrecuenciaResponse.builder()
                .id(f.getId())
                .cooperativaId(f.getCooperativa() != null ? f.getCooperativa().getId() : null)
                .origen(f.getOrigen())
                .destino(f.getDestino())
                .horaSalida(f.getHoraSalida() != null ? f.getHoraSalida().toString() : null) // default HH:mm:ss -> aceptamos HH:mm:ss; cliente puede formatear
                .duracionEstimadaMin(f.getDuracionEstimadaMin())
                .diasOperacion(f.getDiasOperacion())
                .activa(f.getActiva())
                .build();
    }

    private static final List<DateTimeFormatter> TIME_FORMATS = List.of(
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("H'h'mm")
    );

    private static LocalTime parseTime(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String r = raw.trim();
        r = r.replace('.', ':').replace('-', ':');
        for (DateTimeFormatter f : TIME_FORMATS) {
            try {
                return LocalTime.parse(r, f);
            } catch (DateTimeParseException ignored) {}
        }
        if (r.matches("^\\d{1,2}$")) {
            try { return LocalTime.of(Integer.parseInt(r), 0); } catch (Exception ignored) {}
        }
        if (r.matches("^\\d{3,4}$")) {
            try {
                int val = Integer.parseInt(r);
                int hour = val / 100; int min = val % 100;
                if (hour >=0 && hour <= 23 && min >=0 && min <=59) return LocalTime.of(hour, min);
            } catch (Exception ignored) {}
        }
        return null;
    }
}
