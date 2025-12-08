package com.andinobus.backendsmartcode.catalogos.application.services;

import com.andinobus.backendsmartcode.catalogos.api.dto.ParadaCreateRequest;
import com.andinobus.backendsmartcode.catalogos.api.dto.ParadaResponse;
import com.andinobus.backendsmartcode.catalogos.api.dto.ParadaUpdateRequest;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Frecuencia;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Parada;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.FrecuenciaRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.ParadaRepository;
import com.andinobus.backendsmartcode.common.errors.NotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParadaService {

    private final ParadaRepository paradaRepository;
    private final FrecuenciaRepository frecuenciaRepository;

    public ParadaService(ParadaRepository paradaRepository, FrecuenciaRepository frecuenciaRepository) {
        this.paradaRepository = paradaRepository;
        this.frecuenciaRepository = frecuenciaRepository;
    }

    @Transactional
    public ParadaResponse create(Long frecuenciaId, ParadaCreateRequest req) {
        Frecuencia f = frecuenciaRepository.findById(frecuenciaId)
                .orElseThrow(() -> new NotFoundException("Frecuencia no encontrada"));
        if (paradaRepository.existsByFrecuencia_IdAndOrden(f.getId(), req.getOrden())) {
            throw new IllegalArgumentException("Ya existe una parada con ese orden en la frecuencia");
        }
        LocalTime he = parseTime(req.getHoraEstimada());
        Parada p = Parada.builder()
                .frecuencia(f)
                .ciudad(req.getCiudad())
                .orden(req.getOrden())
                .horaEstimada(he)
                .build();
        p = paradaRepository.save(p);
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public List<ParadaResponse> listByFrecuencia(Long frecuenciaId) {
        List<Parada> items = paradaRepository.findByFrecuencia_Id(frecuenciaId, Sort.by(Sort.Direction.ASC, "orden"));
        return items.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ParadaResponse get(Long id) {
        Parada p = paradaRepository.findById(id).orElseThrow(() -> new NotFoundException("Parada no encontrada"));
        return toResponse(p);
    }

    @Transactional
    public ParadaResponse update(Long id, ParadaUpdateRequest req) {
        Parada p = paradaRepository.findById(id).orElseThrow(() -> new NotFoundException("Parada no encontrada"));
        if (req.getOrden() != null && !req.getOrden().equals(p.getOrden())) {
            if (paradaRepository.existsByFrecuencia_IdAndOrden(p.getFrecuencia().getId(), req.getOrden())) {
                throw new IllegalArgumentException("Ya existe una parada con ese orden en la frecuencia");
            }
            p.setOrden(req.getOrden());
        }
        if (req.getCiudad() != null) p.setCiudad(req.getCiudad());
        if (req.getHoraEstimada() != null) {
            LocalTime he = parseTime(req.getHoraEstimada());
            if (he == null && !req.getHoraEstimada().isBlank()) {
                throw new IllegalArgumentException("Formato inválido de horaEstimada. Use HH:mm o equivalente");
            }
            p.setHoraEstimada(he);
        }
        p = paradaRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public void delete(Long id) {
        Parada p = paradaRepository.findById(id).orElseThrow(() -> new NotFoundException("Parada no encontrada"));
        paradaRepository.delete(p);
    }

    private ParadaResponse toResponse(Parada p) {
        return ParadaResponse.builder()
                .id(p.getId())
                .frecuenciaId(p.getFrecuencia() != null ? p.getFrecuencia().getId() : null)
                .ciudad(p.getCiudad())
                .orden(p.getOrden())
                .horaEstimada(p.getHoraEstimada() != null ? p.getHoraEstimada().toString() : null)
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
