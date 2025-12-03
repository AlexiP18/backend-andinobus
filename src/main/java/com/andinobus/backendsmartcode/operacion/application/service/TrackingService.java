package com.andinobus.backendsmartcode.operacion.application.service;

import com.andinobus.backendsmartcode.operacion.application.dto.ActualizarPosicionRequest;
import com.andinobus.backendsmartcode.operacion.application.dto.PosicionViajeDTO;
import com.andinobus.backendsmartcode.operacion.domain.PosicionViaje;
import com.andinobus.backendsmartcode.operacion.domain.entities.Viaje;
import com.andinobus.backendsmartcode.operacion.infrastructure.repositories.PosicionViajeRepository;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingService {

    private final ViajeRepository viajeRepository;
    private final PosicionViajeRepository posicionViajeRepository;

    /**
     * Actualiza la posición GPS de un viaje en curso
     * Este método es llamado desde la app móvil del chofer
     */
    @Transactional
    public PosicionViajeDTO actualizarPosicion(Long viajeId, ActualizarPosicionRequest request) {
        log.info("Actualizando posición del viaje {}: lat={}, lon={}", viajeId, request.getLatitud(), request.getLongitud());

        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado: " + viajeId));

        // Verificar que el viaje está en curso
        if (!viaje.isEnCurso() && !"PROGRAMADO".equals(viaje.getEstado())) {
            throw new RuntimeException("El viaje no está en curso");
        }

        // Si el viaje está PROGRAMADO, iniciar el viaje automáticamente
        if ("PROGRAMADO".equals(viaje.getEstado())) {
            viaje.setEstado("EN_CURSO");
            viaje.setHoraInicioReal(LocalDateTime.now());
            log.info("Viaje {} iniciado automáticamente", viajeId);
        }

        // Crear registro de posición
        PosicionViaje posicion = new PosicionViaje();
        posicion.setViaje(viaje);
        posicion.setLatitud(request.getLatitud());
        posicion.setLongitud(request.getLongitud());
        posicion.setVelocidadKmh(request.getVelocidadKmh());
        posicion.setPrecision(request.getPrecision());
        posicion.setTimestamp(request.getTimestamp());
        posicion.setProvider(request.getProvider());

        posicion = posicionViajeRepository.save(posicion);

        // Actualizar posición actual en el viaje
        viaje.actualizarPosicion(request.getLatitud(), request.getLongitud());
        viajeRepository.save(viaje);

        log.info("Posición actualizada para viaje {}", viajeId);
        return PosicionViajeDTO.fromEntity(posicion);
    }

    /**
     * Obtiene el historial de posiciones de un viaje
     */
    @Transactional(readOnly = true)
    public List<PosicionViajeDTO> obtenerHistorial(Long viajeId, LocalDateTime desde) {
        if (desde != null) {
            return posicionViajeRepository.findByViajeIdAndTimestampAfter(viajeId, desde)
                    .stream()
                    .map(PosicionViajeDTO::fromEntity)
                    .collect(Collectors.toList());
        } else {
            return posicionViajeRepository.findByViajeIdOrderByTimestampDesc(viajeId)
                    .stream()
                    .map(PosicionViajeDTO::fromEntity)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Obtiene la última posición conocida de un viaje
     */
    @Transactional(readOnly = true)
    public PosicionViajeDTO obtenerPosicionActual(Long viajeId) {
        return posicionViajeRepository.findFirstByViajeIdOrderByTimestampDesc(viajeId)
                .map(PosicionViajeDTO::fromEntity)
                .orElse(null);
    }

    /**
     * Inicia un viaje manualmente (desde la app del chofer)
     */
    @Transactional
    public void iniciarViaje(Long viajeId) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado: " + viajeId));

        if (!"PROGRAMADO".equals(viaje.getEstado())) {
            throw new RuntimeException("El viaje ya fue iniciado o no está programado");
        }

        viaje.setEstado("EN_CURSO");
        viaje.setHoraInicioReal(LocalDateTime.now());
        viajeRepository.save(viaje);

        log.info("Viaje {} iniciado manualmente", viajeId);
    }

    /**
     * Finaliza un viaje manualmente (desde la app del chofer)
     */
    @Transactional
    public void finalizarViaje(Long viajeId) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado: " + viajeId));

        if (!viaje.isEnCurso()) {
            throw new RuntimeException("El viaje no está en curso");
        }

        viaje.setEstado("FINALIZADO");
        viaje.setHoraFinReal(LocalDateTime.now());
        viajeRepository.save(viaje);

        log.info("Viaje {} finalizado", viajeId);
    }

    /**
     * Verifica si el chofer tiene permiso para actualizar este viaje
     */
    public boolean choferTienePermisoParaViaje(Long viajeId, Long choferId) {
        return viajeRepository.findById(viajeId)
                .map(viaje -> viaje.getChofer() != null && viaje.getChofer().getId().equals(choferId))
                .orElse(false);
    }
}
