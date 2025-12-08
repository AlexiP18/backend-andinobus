package com.andinobus.backendsmartcode.operacion.application.services;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Frecuencia;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.BusAsientoConfigRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.FrecuenciaRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusRepository;
import com.andinobus.backendsmartcode.operacion.api.dto.ViajeDtos.*;
import com.andinobus.backendsmartcode.operacion.domain.entities.Viaje;
import com.andinobus.backendsmartcode.operacion.domain.entities.ViajeAsiento;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeAsientoRepository;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ViajeService {

    private final ViajeRepository viajeRepository;
    private final ViajeAsientoRepository viajeAsientoRepository;
    private final FrecuenciaRepository frecuenciaRepository;
    private final BusRepository busRepository;
    private final BusAsientoConfigRepository busAsientoConfigRepository;
    private final ViajeAsientoService viajeAsientoService;

    /**
     * Crea viajes programados para una fecha y frecuencia específica
     */
    public Viaje crearViaje(Long frecuenciaId, Long busId, LocalDate fecha) {
        Frecuencia frecuencia = frecuenciaRepository.findById(frecuenciaId)
                .orElseThrow(() -> new RuntimeException("Frecuencia no encontrada"));

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new RuntimeException("Bus no encontrado"));

        // Verificar si ya existe un viaje para esta frecuencia en esta fecha
        List<Viaje> viajesExistentes = viajeRepository.findByFrecuenciaIdAndFecha(frecuenciaId, fecha);
        if (!viajesExistentes.isEmpty()) {
            return viajesExistentes.get(0);
        }

        // Crear nuevo viaje
        Viaje viaje = Viaje.builder()
                .frecuencia(frecuencia)
                .bus(bus)
                .fecha(fecha)
                .horaSalidaProgramada(frecuencia.getHoraSalida())
                .estado("PROGRAMADO")
                .build();

        viaje = viajeRepository.save(viaje);
        
        // Inicializar asientos del viaje desde el layout del bus
        try {
            viajeAsientoService.inicializarAsientosViaje(viaje.getId());
        } catch (Exception e) {
            // No lanzar excepción, el viaje puede funcionar sin configuración de asientos
        }
        
        return viaje;
    }

    /**
     * Programa viajes automáticamente para los próximos días
     */
    public void programarViajesParaLosSiguientesDias(int dias) {
        LocalDate hoy = LocalDate.now();
        List<Frecuencia> frecuenciasActivas = frecuenciaRepository.findByActivaTrue();
        List<Bus> busesDisponibles = busRepository.findByActivoTrue();

        if (busesDisponibles.isEmpty()) {
            throw new RuntimeException("No hay buses disponibles");
        }

        for (int i = 0; i < dias; i++) {
            LocalDate fecha = hoy.plusDays(i);
            
            for (Frecuencia frecuencia : frecuenciasActivas) {
                // Asignar un bus disponible (lógica simple: round-robin)
                Bus bus = busesDisponibles.get(frecuenciasActivas.indexOf(frecuencia) % busesDisponibles.size());
                
                // Crear viaje si no existe
                crearViaje(frecuencia.getId(), bus.getId(), fecha);
            }
        }
    }

    /**
     * Obtiene los viajes disponibles de una cooperativa para una fecha
     */
    public List<ViajeDisponibleResponse> getViajesDisponibles(Long cooperativaId, LocalDate fecha) {
        List<Viaje> viajes = viajeRepository.findByCooperativaIdAndFecha(cooperativaId, fecha);
        
        return viajes.stream()
                .map(this::mapToViajeDisponible)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el detalle de un viaje con sus asientos
     */
    public ViajeDetalleResponse getViajeDetalle(Long viajeId) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));

        List<ViajeAsiento> asientos = viajeAsientoRepository.findByViajeId(viajeId);
        
        return ViajeDetalleResponse.builder()
                .id(viaje.getId())
                .origen(viaje.getFrecuencia().getOrigen())
                .destino(viaje.getFrecuencia().getDestino())
                .fecha(viaje.getFecha())
                .horaSalida(viaje.getHoraSalidaProgramada())
                .horaLlegadaEstimada(viaje.getHoraLlegadaEstimada())
                .busPlaca(viaje.getBus().getPlaca())
                .busMarca(viaje.getBus().getChasisMarca())
                .capacidadTotal(viaje.getBus().getCapacidadAsientos())
                .asientosDisponibles(contarAsientosDisponibles(asientos))
                .precioBase(getPrecioBase())
                .estado(viaje.getEstado())
                .asientos(asientos.stream()
                        .map(this::mapToAsientoResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Mapea un viaje a DTO de viaje disponible
     */
    private ViajeDisponibleResponse mapToViajeDisponible(Viaje viaje) {
        Long asientosDisponibles = viajeAsientoRepository.countDisponiblesByViajeId(viaje.getId());
        
        return ViajeDisponibleResponse.builder()
                .id(viaje.getId())
                .origen(viaje.getFrecuencia().getOrigen())
                .destino(viaje.getFrecuencia().getDestino())
                .fecha(viaje.getFecha())
                .horaSalida(viaje.getHoraSalidaProgramada())
                .busPlaca(viaje.getBus().getPlaca())
                .capacidadTotal(viaje.getBus().getCapacidadAsientos())
                .asientosDisponibles(asientosDisponibles.intValue())
                .precioBase(getPrecioBase())
                .estado(viaje.getEstado())
                .build();
    }

    /**
     * Mapea un asiento a DTO de respuesta
     */
    private AsientoResponse mapToAsientoResponse(ViajeAsiento asiento) {
        return AsientoResponse.builder()
                .id(asiento.getId())
                .numeroAsiento(asiento.getNumeroAsiento())
                .tipoAsiento(asiento.getTipoAsiento())
                .estado(asiento.getEstado())
                .reservaId(asiento.getReserva() != null ? asiento.getReserva().getId() : null)
                .build();
    }

    /**
     * Cuenta asientos disponibles de una lista
     */
    private Integer contarAsientosDisponibles(List<ViajeAsiento> asientos) {
        return (int) asientos.stream()
                .filter(a -> "DISPONIBLE".equals(a.getEstado()))
                .count();
    }

    /**
     * Obtiene el precio base (puede ser configurado por cooperativa en el futuro)
     */
    private BigDecimal getPrecioBase() {
        // TODO: Implementar lógica de precios por cooperativa/ruta
        return new BigDecimal("15.50");
    }
}
