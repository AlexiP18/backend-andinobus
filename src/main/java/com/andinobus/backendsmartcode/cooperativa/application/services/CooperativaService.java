package com.andinobus.backendsmartcode.cooperativa.application.services;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Frecuencia;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.FrecuenciaRepository;
import com.andinobus.backendsmartcode.operacion.domain.entities.AsignacionBusFrecuencia;
import com.andinobus.backendsmartcode.operacion.domain.entities.DiaParadaBus;
import com.andinobus.backendsmartcode.operacion.domain.repositories.AsignacionBusFrecuenciaRepository;
import com.andinobus.backendsmartcode.operacion.domain.repositories.DiaParadaBusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar operaciones de la Cooperativa:
 * - Gestión de buses (estado, disponibilidad)
 * - Asignación de buses a frecuencias
 * - Gestión de días de parada
 * - Generación de hojas de trabajo
 */
@Service("cooperativaOpsService")
@Profile("dev")
@RequiredArgsConstructor
public class CooperativaService {

    private final BusRepository busRepository;
    private final FrecuenciaRepository frecuenciaRepository;
    private final AsignacionBusFrecuenciaRepository asignacionRepository;
    private final DiaParadaBusRepository diaParadaRepository;

    /**
     * Obtener todos los buses de una cooperativa
     */
    @Transactional(readOnly = true)
    public List<Bus> obtenerBusesCooperativa(Long cooperativaId) {
        return busRepository.findByCooperativaId(cooperativaId);
    }

    /**
     * Obtener buses disponibles (sin asignación activa) para una fecha específica
     */
    @Transactional(readOnly = true)
    public List<Bus> obtenerBusesDisponibles(Long cooperativaId, LocalDate fecha) {
        List<Bus> todosBuses = busRepository.findByCooperativaIdAndActivoTrue(cooperativaId);
        
        return todosBuses.stream()
                .filter(bus -> {
                    // Verificar que no esté en día de parada
                    Optional<DiaParadaBus> parada = diaParadaRepository.findByBusIdAndFecha(bus.getId(), fecha);
                    if (parada.isPresent()) return false;
                    
                    // Verificar que no tenga asignación activa
                    Optional<AsignacionBusFrecuencia> asignacion = 
                            asignacionRepository.findAsignacionActivaByBusAndFecha(bus.getId(), fecha);
                    return asignacion.isEmpty();
                })
                .toList();
    }

    /**
     * Asignar un bus a una frecuencia
     */
    @Transactional
    public AsignacionBusFrecuencia asignarBusAFrecuencia(
            Long busId, 
            Long frecuenciaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin,
            String observaciones) {
        
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new IllegalArgumentException("Bus no encontrado"));
        
        Frecuencia frecuencia = frecuenciaRepository.findById(frecuenciaId)
                .orElseThrow(() -> new IllegalArgumentException("Frecuencia no encontrada"));

        // Verificar que el bus pertenece a la misma cooperativa que la frecuencia
        if (!bus.getCooperativa().getId().equals(frecuencia.getCooperativa().getId())) {
            throw new IllegalArgumentException("El bus y la frecuencia deben pertenecer a la misma cooperativa");
        }

        // Crear la asignación
        AsignacionBusFrecuencia asignacion = AsignacionBusFrecuencia.builder()
                .bus(bus)
                .frecuencia(frecuencia)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .estado("ACTIVA")
                .observaciones(observaciones)
                .build();

        // Actualizar estado del bus
        bus.setEstado("EN_SERVICIO");
        busRepository.save(bus);

        return asignacionRepository.save(asignacion);
    }

    /**
     * Finalizar una asignación de bus a frecuencia
     */
    @Transactional
    public void finalizarAsignacion(Long asignacionId) {
        AsignacionBusFrecuencia asignacion = asignacionRepository.findById(asignacionId)
                .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada"));
        
        asignacion.setEstado("FINALIZADA");
        asignacion.setFechaFin(LocalDate.now());
        asignacionRepository.save(asignacion);

        // Actualizar estado del bus a DISPONIBLE
        Bus bus = asignacion.getBus();
        bus.setEstado("DISPONIBLE");
        busRepository.save(bus);
    }

    /**
     * Registrar un día de parada para un bus
     */
    @Transactional
    public DiaParadaBus registrarDiaParada(Long busId, LocalDate fecha, String motivo, String observaciones) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new IllegalArgumentException("Bus no encontrado"));

        // Verificar que no exista ya un día de parada para esa fecha
        Optional<DiaParadaBus> paradaExistente = diaParadaRepository.findByBusIdAndFecha(busId, fecha);
        if (paradaExistente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un día de parada registrado para esta fecha");
        }

        DiaParadaBus diaParada = DiaParadaBus.builder()
                .bus(bus)
                .fecha(fecha)
                .motivo(motivo)
                .observaciones(observaciones)
                .build();

        return diaParadaRepository.save(diaParada);
    }

    /**
     * Obtener asignaciones activas para una cooperativa
     */
    @Transactional(readOnly = true)
    public List<AsignacionBusFrecuencia> obtenerAsignacionesActivas(Long cooperativaId) {
        return asignacionRepository.findByEstado("ACTIVA").stream()
                .filter(a -> a.getBus().getCooperativa().getId().equals(cooperativaId))
                .toList();
    }

    /**
     * Obtener días de parada para un rango de fechas
     */
    @Transactional(readOnly = true)
    public List<DiaParadaBus> obtenerDiasParada(Long cooperativaId, LocalDate fechaInicio, LocalDate fechaFin) {
        return diaParadaRepository.findByFechaRange(fechaInicio, fechaFin).stream()
                .filter(d -> d.getBus().getCooperativa().getId().equals(cooperativaId))
                .toList();
    }

    /**
     * Calcular exceso de buses (buses sin asignación)
     */
    @Transactional(readOnly = true)
    public int calcularExcesoBuses(Long cooperativaId, LocalDate fecha) {
        return obtenerBusesDisponibles(cooperativaId, fecha).size();
    }
}
