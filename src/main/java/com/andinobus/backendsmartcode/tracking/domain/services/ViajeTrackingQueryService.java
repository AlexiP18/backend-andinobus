package com.andinobus.backendsmartcode.tracking.domain.services;

import com.andinobus.backendsmartcode.tracking.application.dto.ViajeActivoDTO;
import com.andinobus.backendsmartcode.operacion.domain.entities.Viaje;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeRepository;
import com.andinobus.backendsmartcode.ventas.domain.repositories.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para consultar información de viajes activos
 * Usado por los dashboards de tracking
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ViajeTrackingQueryService {

    private final ViajeRepository viajeRepository;
    private final ReservaRepository reservaRepository;

    /**
     * Obtiene todos los viajes activos del sistema
     * Para Super Admin
     */
    public List<ViajeActivoDTO> obtenerViajesActivos() {
        log.info("Obteniendo todos los viajes activos del sistema");

        LocalDate hoy = LocalDate.now();
        List<Viaje> viajes = new ArrayList<>();
        viajes.addAll(viajeRepository.findActivosByFecha(hoy.minusDays(1)));
        viajes.addAll(viajeRepository.findActivosByFecha(hoy));
        viajes.addAll(viajeRepository.findActivosByFecha(hoy.plusDays(1)));

        return viajes.stream()
                .filter(v -> "EN_CURSO".equals(v.getEstado()) ||
                             "PROGRAMADO".equals(v.getEstado()) ||
                             "EN_TERMINAL".equals(v.getEstado()))
                .map(this::convertirAViajeActivoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene viajes activos de una cooperativa específica
     * Para Admin de Cooperativa
     */
    public List<ViajeActivoDTO> obtenerViajesActivosPorCooperativa(Long cooperativaId) {
        log.info("Obteniendo viajes activos de la cooperativa: {}", cooperativaId);

        LocalDate hoy = LocalDate.now();
        List<Viaje> viajes = new ArrayList<>();
        viajes.addAll(viajeRepository.findActivosByFecha(hoy.minusDays(1)));
        viajes.addAll(viajeRepository.findActivosByFecha(hoy));
        viajes.addAll(viajeRepository.findActivosByFecha(hoy.plusDays(1)));

        return viajes.stream()
                .filter(v -> v.getFrecuencia() != null && v.getFrecuencia().getCooperativa() != null
                        && cooperativaId.equals(v.getFrecuencia().getCooperativa().getId()))
                .filter(v -> "EN_CURSO".equals(v.getEstado()) ||
                             "PROGRAMADO".equals(v.getEstado()) ||
                             "EN_TERMINAL".equals(v.getEstado()))
                .map(this::convertirAViajeActivoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene viajes activos para un cliente específico
     * Basado en los boletos que ha comprado
     */
    public List<ViajeActivoDTO> obtenerViajesActivosPorCliente(String emailCliente) {
        log.info("Obteniendo viajes activos del cliente: {}", emailCliente);

        // Obtener IDs de viajes con reservas del cliente en estado PAGADO
        List<Long> viajeIds = reservaRepository.findByClienteEmail(emailCliente).stream()
                .filter(r -> "PAGADO".equals(r.getEstado()))
                .map(r -> r.getViaje().getId())
                .distinct()
                .collect(Collectors.toList());

        if (viajeIds.isEmpty()) {
            return List.of();
        }

        // Obtener viajes de esos IDs
        List<Viaje> viajes = viajeRepository.findAllById(viajeIds);

        return viajes.stream()
                .filter(v -> "EN_CURSO".equals(v.getEstado()) ||
                             "PROGRAMADO".equals(v.getEstado()))
                .map(this::convertirAViajeActivoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el detalle completo de un viaje específico
     */
    public ViajeActivoDTO obtenerDetalleViaje(Long viajeId) {
        log.info("Obteniendo detalle del viaje: {}", viajeId);

        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado: " + viajeId));

        return convertirAViajeActivoDTO(viaje);
    }

    /**
     * Convierte una entidad Viaje a ViajeActivoDTO
     */
    private ViajeActivoDTO convertirAViajeActivoDTO(Viaje viaje) {
        var frecuencia = viaje.getFrecuencia();
        var bus = viaje.getBus();
        var cooperativa = (bus != null) ? bus.getCooperativa() : null;
        var chofer = viaje.getChofer();

        // Contar pasajeros (reservas pagadas) sumando asientos
        int numeroPasajeros = reservaRepository.findByViajeId(viaje.getId()).stream()
                .filter(r -> "PAGADO".equals(r.getEstado()))
                .mapToInt(r -> r.getAsientos() != null ? r.getAsientos() : 0)
                .sum();

        return ViajeActivoDTO.builder()
                .id(viaje.getId())
                .viajeId(viaje.getId())
                // Bus
                .busPlaca(bus != null ? bus.getPlaca() : null)
                .busId(bus != null ? bus.getId() : null)
                // Cooperativa
                .cooperativaNombre(cooperativa != null ? cooperativa.getNombre() : null)
                .cooperativaId(cooperativa != null ? cooperativa.getId() : null)
                // Ruta / Frecuencia
                .rutaOrigen(frecuencia != null ? frecuencia.getOrigen() : null)
                .rutaDestino(frecuencia != null ? frecuencia.getDestino() : null)
                .rutaNombre(frecuencia != null ? (frecuencia.getOrigen() + " - " + frecuencia.getDestino()) : null)
                // Chofer
                .choferNombre(chofer != null ? chofer.getNombres() : "Sin asignar")
                .choferApellido(chofer != null ? chofer.getApellidos() : "")
                .choferId(chofer != null ? chofer.getId() : null)
                // Viaje
                .fechaSalida(viaje.getFecha() != null ? viaje.getFecha().format(DateTimeFormatter.ISO_LOCAL_DATE) : null)
                .horaSalida(viaje.getHoraSalida() != null ? viaje.getHoraSalida().toString() : null)
                .horaLlegadaEstimada(viaje.getHoraLlegadaEstimada() != null ? viaje.getHoraLlegadaEstimada().toString() : null)
                .estado(viaje.getEstado())
                // Capacidad
                .numeroPasajeros(numeroPasajeros)
                .capacidadTotal(bus != null ? bus.getCapacidadAsientos() : null)
                // Posición GPS (convertir Double -> BigDecimal)
                .latitudActual(viaje.getLatitudActual() != null ? BigDecimal.valueOf(viaje.getLatitudActual()) : null)
                .longitudActual(viaje.getLongitudActual() != null ? BigDecimal.valueOf(viaje.getLongitudActual()) : null)
                .velocidadKmh(null) // Se podría obtener de la última PosicionViaje
                .ultimaActualizacion(viaje.getUltimaActualizacion())
                // Tiempos reales
                .horaInicioReal(viaje.getHoraInicioReal())
                .horaFinReal(viaje.getHoraFinReal())
                .build();
    }
}
