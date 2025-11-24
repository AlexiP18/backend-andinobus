package com.andinobus.backendsmartcode.admin.application.services;

import com.andinobus.backendsmartcode.admin.application.dtos.FrecuenciaDtos.*;
import com.andinobus.backendsmartcode.admin.domain.entities.FrecuenciaViaje;
import com.andinobus.backendsmartcode.admin.domain.entities.ParadaFrecuencia;
import com.andinobus.backendsmartcode.admin.domain.entities.Ruta;
import com.andinobus.backendsmartcode.admin.domain.repositories.FrecuenciaViajeRepository;
import com.andinobus.backendsmartcode.admin.domain.repositories.ParadaFrecuenciaRepository;
import com.andinobus.backendsmartcode.admin.domain.repositories.RutaRepository;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Profile("dev")
@Service
@RequiredArgsConstructor
public class FrecuenciaViajeService {

    private final FrecuenciaViajeRepository frecuenciaRepository;
    private final ParadaFrecuenciaRepository paradaRepository;
    private final BusRepository busRepository;
    private final RutaRepository rutaRepository;

    @Transactional
    public List<FrecuenciaViajeResponse> getAllByBus(Long busId) {
        return frecuenciaRepository.findByBusIdOrderByHoraSalida(busId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<FrecuenciaViajeResponse> getAllByCooperativa(Long cooperativaId) {
        return frecuenciaRepository.findByCooperativaIdOrderByHoraSalida(cooperativaId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FrecuenciaViajeResponse getById(Long id) {
        FrecuenciaViaje frecuencia = frecuenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Frecuencia no encontrada con id: " + id));
        return toResponse(frecuencia);
    }

    @Transactional
    public FrecuenciaViajeResponse create(CreateFrecuenciaRequest request) {
        // Validar bus
        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new RuntimeException("Bus no encontrado con id: " + request.getBusId()));

        // Validar ruta
        Ruta ruta = rutaRepository.findById(request.getRutaId())
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada con id: " + request.getRutaId()));

        // Validar que no exista frecuencia duplicada
        if (frecuenciaRepository.existsByBusIdAndRutaIdAndHoraSalidaAndActivoTrue(
                request.getBusId(), request.getRutaId(), request.getHoraSalida())) {
            throw new RuntimeException("Ya existe una frecuencia para este bus en esta ruta a la misma hora");
        }

        // Crear frecuencia
        FrecuenciaViaje frecuencia = FrecuenciaViaje.builder()
                .bus(bus)
                .ruta(ruta)
                .horaSalida(request.getHoraSalida())
                .horaLlegadaEstimada(request.getHoraLlegadaEstimada())
                .diasOperacion(request.getDiasOperacion())
                .precioBase(request.getPrecioBase())
                .asientosDisponibles(request.getAsientosDisponibles() != null 
                    ? request.getAsientosDisponibles() 
                    : bus.getCapacidadAsientos())
                .observaciones(request.getObservaciones())
                .activo(true)
                .build();

        frecuencia = frecuenciaRepository.save(frecuencia);

        // Crear paradas si existen
        if (request.getParadas() != null && !request.getParadas().isEmpty()) {
            for (CreateParadaRequest paradaReq : request.getParadas()) {
                ParadaFrecuencia parada = ParadaFrecuencia.builder()
                        .frecuenciaViaje(frecuencia)
                        .orden(paradaReq.getOrden())
                        .nombreParada(paradaReq.getNombreParada())
                        .direccion(paradaReq.getDireccion())
                        .tiempoLlegada(paradaReq.getTiempoLlegada())
                        .tiempoEsperaMinutos(paradaReq.getTiempoEsperaMinutos() != null 
                            ? paradaReq.getTiempoEsperaMinutos() 
                            : 5)
                        .precioDesdeOrigen(paradaReq.getPrecioDesdeOrigen())
                        .observaciones(paradaReq.getObservaciones())
                        .permiteAbordaje(paradaReq.getPermiteAbordaje() != null 
                            ? paradaReq.getPermiteAbordaje() 
                            : true)
                        .permiteDescenso(paradaReq.getPermiteDescenso() != null 
                            ? paradaReq.getPermiteDescenso() 
                            : true)
                        .build();
                
                frecuencia.addParada(parada);
            }
            frecuencia = frecuenciaRepository.save(frecuencia);
        }

        return toResponse(frecuencia);
    }

    @Transactional
    public FrecuenciaViajeResponse update(Long id, UpdateFrecuenciaRequest request) {
        FrecuenciaViaje frecuencia = frecuenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Frecuencia no encontrada con id: " + id));

        // Actualizar campos básicos
        if (request.getHoraSalida() != null) {
            frecuencia.setHoraSalida(request.getHoraSalida());
        }
        if (request.getHoraLlegadaEstimada() != null) {
            frecuencia.setHoraLlegadaEstimada(request.getHoraLlegadaEstimada());
        }
        if (request.getDiasOperacion() != null) {
            frecuencia.setDiasOperacion(request.getDiasOperacion());
        }
        if (request.getPrecioBase() != null) {
            frecuencia.setPrecioBase(request.getPrecioBase());
        }
        if (request.getAsientosDisponibles() != null) {
            frecuencia.setAsientosDisponibles(request.getAsientosDisponibles());
        }
        if (request.getObservaciones() != null) {
            frecuencia.setObservaciones(request.getObservaciones());
        }

        // Actualizar paradas si se proporcionan
        if (request.getParadas() != null) {
            // Eliminar paradas existentes
            frecuencia.getParadas().clear();
            paradaRepository.deleteByFrecuenciaViajeId(id);

            // Agregar nuevas paradas
            for (CreateParadaRequest paradaReq : request.getParadas()) {
                ParadaFrecuencia parada = ParadaFrecuencia.builder()
                        .frecuenciaViaje(frecuencia)
                        .orden(paradaReq.getOrden())
                        .nombreParada(paradaReq.getNombreParada())
                        .direccion(paradaReq.getDireccion())
                        .tiempoLlegada(paradaReq.getTiempoLlegada())
                        .tiempoEsperaMinutos(paradaReq.getTiempoEsperaMinutos() != null 
                            ? paradaReq.getTiempoEsperaMinutos() 
                            : 5)
                        .precioDesdeOrigen(paradaReq.getPrecioDesdeOrigen())
                        .observaciones(paradaReq.getObservaciones())
                        .permiteAbordaje(paradaReq.getPermiteAbordaje() != null 
                            ? paradaReq.getPermiteAbordaje() 
                            : true)
                        .permiteDescenso(paradaReq.getPermiteDescenso() != null 
                            ? paradaReq.getPermiteDescenso() 
                            : true)
                        .build();
                
                frecuencia.addParada(parada);
            }
        }

        frecuencia = frecuenciaRepository.save(frecuencia);
        return toResponse(frecuencia);
    }

    @Transactional
    public void delete(Long id) {
        FrecuenciaViaje frecuencia = frecuenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Frecuencia no encontrada con id: " + id));
        frecuencia.setActivo(false);
        frecuenciaRepository.save(frecuencia);
    }

    private FrecuenciaViajeResponse toResponse(FrecuenciaViaje frecuencia) {
        List<ParadaResponse> paradas = frecuencia.getParadas().stream()
                .map(p -> ParadaResponse.builder()
                        .id(p.getId())
                        .orden(p.getOrden())
                        .nombreParada(p.getNombreParada())
                        .direccion(p.getDireccion())
                        .tiempoLlegada(p.getTiempoLlegada())
                        .tiempoEsperaMinutos(p.getTiempoEsperaMinutos())
                        .precioDesdeOrigen(p.getPrecioDesdeOrigen())
                        .observaciones(p.getObservaciones())
                        .permiteAbordaje(p.getPermiteAbordaje())
                        .permiteDescenso(p.getPermiteDescenso())
                        .build())
                .collect(Collectors.toList());

        return FrecuenciaViajeResponse.builder()
                .id(frecuencia.getId())
                .busId(frecuencia.getBus().getId())
                .busPlaca(frecuencia.getBus().getPlaca())
                .rutaId(frecuencia.getRuta().getId())
                .rutaNombre(frecuencia.getRuta().getNombre())
                .rutaOrigen(frecuencia.getRuta().getOrigen())
                .rutaDestino(frecuencia.getRuta().getDestino())
                .horaSalida(frecuencia.getHoraSalida())
                .horaLlegadaEstimada(frecuencia.getHoraLlegadaEstimada())
                .diasOperacion(frecuencia.getDiasOperacion())
                .precioBase(frecuencia.getPrecioBase())
                .asientosDisponibles(frecuencia.getAsientosDisponibles())
                .observaciones(frecuencia.getObservaciones())
                .activo(frecuencia.getActivo())
                .paradas(paradas)
                .build();
    }
}
