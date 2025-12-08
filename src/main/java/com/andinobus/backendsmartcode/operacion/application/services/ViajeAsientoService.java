package com.andinobus.backendsmartcode.operacion.application.services;

import com.andinobus.backendsmartcode.catalogos.domain.entities.AsientoLayout;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.AsientoLayoutRepository;
import com.andinobus.backendsmartcode.common.errors.NotFoundException;
import com.andinobus.backendsmartcode.operacion.domain.entities.Viaje;
import com.andinobus.backendsmartcode.operacion.domain.entities.ViajeAsiento;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeAsientoRepository;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViajeAsientoService {

    private final ViajeAsientoRepository viajeAsientoRepository;
    private final ViajeRepository viajeRepository;
    private final AsientoLayoutRepository asientoLayoutRepository;

    /**
     * Inicializa los asientos de un viaje basándose en el layout del bus asignado.
     * Solo se ejecuta si el viaje no tiene asientos ya creados.
     */
    @Transactional
    public void inicializarAsientosViaje(Long viajeId) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new NotFoundException("Viaje no encontrado con id: " + viajeId));

        // Verificar si ya tiene asientos creados
        long asientosExistentes = viajeAsientoRepository.countByViajeId(viajeId);
        if (asientosExistentes > 0) {
            log.info("El viaje {} ya tiene {} asientos inicializados", viajeId, asientosExistentes);
            return;
        }

        Long busId = viaje.getBus().getId();

        // Obtener el layout de asientos del bus
        List<AsientoLayout> layoutAsientos = asientoLayoutRepository.findByBusIdOrderByNumeroAsientoAsc(busId);

        if (layoutAsientos.isEmpty()) {
            log.warn("El bus {} no tiene layout de asientos configurado. No se inicializarán asientos para el viaje {}", busId, viajeId);
            return;
        }

        // Crear ViajeAsiento para cada asiento del layout que esté habilitado
        List<ViajeAsiento> viajeAsientos = layoutAsientos.stream()
                .filter(asiento -> Boolean.TRUE.equals(asiento.getHabilitado()))
                .map(asiento -> ViajeAsiento.builder()
                        .viaje(viaje)
                        .numeroAsiento(String.valueOf(asiento.getNumeroAsiento()))
                        .tipoAsiento(asiento.getTipoAsiento())
                        .estado("DISPONIBLE")
                        .build())
                .collect(Collectors.toList());

        viajeAsientoRepository.saveAll(viajeAsientos);
        log.info("Inicializados {} asientos para el viaje {}", viajeAsientos.size(), viajeId);
    }

    /**
     * Obtiene todos los asientos de un viaje con su estado actual.
     */
    @Transactional(readOnly = true)
    public List<ViajeAsiento> obtenerAsientosViaje(Long viajeId) {
        // Verificar que el viaje existe
        if (!viajeRepository.existsById(viajeId)) {
            throw new NotFoundException("Viaje no encontrado con id: " + viajeId);
        }

        return viajeAsientoRepository.findByViajeIdOrderByNumeroAsientoAsc(viajeId);
    }

    /**
     * Obtiene solo los asientos disponibles de un viaje.
     */
    @Transactional(readOnly = true)
    public List<ViajeAsiento> obtenerAsientosDisponibles(Long viajeId) {
        return viajeAsientoRepository.findByViajeIdAndEstadoOrderByNumeroAsientoAsc(viajeId, "DISPONIBLE");
    }

    /**
     * Reserva asientos específicos para una reserva.
     * Valida que los asientos estén disponibles antes de reservarlos.
     */
    @Transactional
    public void reservarAsientos(Long viajeId, List<String> numerosAsiento) {
        List<ViajeAsiento> asientos = viajeAsientoRepository.findByViajeIdAndNumeroAsientoIn(viajeId, numerosAsiento);

        if (asientos.size() != numerosAsiento.size()) {
            throw new IllegalArgumentException("Algunos asientos no existen en este viaje");
        }

        // Validar que todos estén disponibles
        List<String> noDisponibles = asientos.stream()
                .filter(a -> !"DISPONIBLE".equals(a.getEstado()))
                .map(ViajeAsiento::getNumeroAsiento)
                .collect(Collectors.toList());

        if (!noDisponibles.isEmpty()) {
            throw new IllegalStateException("Los siguientes asientos no están disponibles: " + String.join(", ", noDisponibles));
        }

        // Marcar como reservados
        asientos.forEach(asiento -> asiento.setEstado("RESERVADO"));
        viajeAsientoRepository.saveAll(asientos);

        log.info("Reservados {} asientos para el viaje {}", asientos.size(), viajeId);
    }

    /**
     * Libera asientos reservados (por ejemplo, cuando expira una reserva).
     */
    @Transactional
    public void liberarAsientos(Long viajeId, List<String> numerosAsiento) {
        List<ViajeAsiento> asientos = viajeAsientoRepository.findByViajeIdAndNumeroAsientoIn(viajeId, numerosAsiento);

        asientos.forEach(asiento -> {
            if ("RESERVADO".equals(asiento.getEstado())) {
                asiento.setEstado("DISPONIBLE");
                asiento.setReserva(null);
            }
        });

        viajeAsientoRepository.saveAll(asientos);
        log.info("Liberados {} asientos del viaje {}", asientos.size(), viajeId);
    }

    /**
     * Marca asientos como vendidos cuando se confirma el pago.
     */
    @Transactional
    public void confirmarVentaAsientos(Long viajeId, List<String> numerosAsiento) {
        List<ViajeAsiento> asientos = viajeAsientoRepository.findByViajeIdAndNumeroAsientoIn(viajeId, numerosAsiento);

        asientos.forEach(asiento -> asiento.setEstado("VENDIDO"));
        viajeAsientoRepository.saveAll(asientos);

        log.info("Confirmados {} asientos como vendidos para el viaje {}", asientos.size(), viajeId);
    }

    /**
     * Obtiene estadísticas de ocupación de un viaje.
     */
    @Transactional(readOnly = true)
    public AsientosEstadisticas obtenerEstadisticas(Long viajeId) {
        long total = viajeAsientoRepository.countByViajeId(viajeId);
        long disponibles = viajeAsientoRepository.countByViajeIdAndEstado(viajeId, "DISPONIBLE");
        long reservados = viajeAsientoRepository.countByViajeIdAndEstado(viajeId, "RESERVADO");
        long vendidos = viajeAsientoRepository.countByViajeIdAndEstado(viajeId, "VENDIDO");

        return new AsientosEstadisticas(total, disponibles, reservados, vendidos);
    }

    public record AsientosEstadisticas(
            long total,
            long disponibles,
            long reservados,
            long vendidos
    ) {}
}
