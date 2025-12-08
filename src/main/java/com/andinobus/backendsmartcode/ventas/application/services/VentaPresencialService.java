package com.andinobus.backendsmartcode.ventas.application.services;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Frecuencia;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.FrecuenciaRepository;
import com.andinobus.backendsmartcode.admin.domain.entities.FrecuenciaViaje;
import com.andinobus.backendsmartcode.admin.domain.repositories.FrecuenciaViajeRepository;
import com.andinobus.backendsmartcode.operacion.application.services.ViajeAsientoService;
import com.andinobus.backendsmartcode.operacion.domain.entities.Viaje;
import com.andinobus.backendsmartcode.operacion.domain.entities.ViajeAsiento;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeAsientoRepository;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeRepository;
import com.andinobus.backendsmartcode.ventas.api.dto.VentaPresencialDtos.*;
import com.andinobus.backendsmartcode.ventas.domain.entities.Reserva;
import com.andinobus.backendsmartcode.ventas.domain.repositories.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentaPresencialService {

    private final ReservaRepository reservaRepository;
    private final FrecuenciaViajeRepository frecuenciaViajeRepository;
    private final ViajeRepository viajeRepository;
    private final FrecuenciaRepository frecuenciaRepository;
    private final ViajeAsientoRepository viajeAsientoRepository;
    private final ViajeAsientoService viajeAsientoService;

    /**
     * Crea una venta presencial directa desde una frecuencia
     * El oficinista selecciona una frecuencia de su cooperativa y vende boletos
     */
    @Transactional
    public VentaPresencialResponse crearVentaPresencialDesdeFrecuencia(CreateVentaPresencialRequest request) {
        // 1. Validar que la frecuencia existe y pertenece a la cooperativa
        FrecuenciaViaje frecuencia = frecuenciaViajeRepository.findById(request.getFrecuenciaId())
                .orElseThrow(() -> new RuntimeException("Frecuencia no encontrada"));

        if (!frecuencia.getBus().getCooperativa().getId().equals(request.getCooperativaId())) {
            throw new RuntimeException("La frecuencia no pertenece a esta cooperativa");
        }

        if (!frecuencia.getActivo()) {
            throw new RuntimeException("La frecuencia no está activa");
        }

        // 2. Validar que la fecha corresponde a un día de operación
        LocalDate fechaViaje = LocalDate.parse(request.getFecha());
        String diaSemana = fechaViaje.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("es", "ES"))
                .toUpperCase();

        if (frecuencia.getDiasOperacion() != null && !frecuencia.getDiasOperacion().contains(diaSemana)) {
            throw new RuntimeException("La frecuencia no opera el día " + diaSemana);
        }

        // 3. Validar asientos
        if (request.getAsientos() == null || request.getAsientos().isEmpty()) {
            throw new RuntimeException("Debe seleccionar al menos un asiento");
        }

        Bus bus = frecuencia.getBus();
        int capacidadTotal = bus.getCapacidadAsientos() != null ? bus.getCapacidadAsientos() : 40;

        // Validar que no se excedan los asientos disponibles
        if (request.getAsientos().size() > capacidadTotal) {
            throw new RuntimeException("La cantidad de asientos excede la capacidad del bus");
        }

        // 4. Obtener o crear viaje para esta frecuencia+fecha
        Viaje viaje = obtenerOCrearViaje(frecuencia, fechaViaje);

        // 4.1 Validar y marcar asientos como VENDIDOS
        for (String numeroAsiento : request.getAsientos()) {
            ViajeAsiento asiento = viajeAsientoRepository
                    .findByViajeIdAndNumeroAsiento(viaje.getId(), numeroAsiento)
                    .orElse(null);

            if (asiento == null) {
                // Crear asiento si no existe
                asiento = ViajeAsiento.builder()
                        .viaje(viaje)
                        .numeroAsiento(numeroAsiento)
                        .tipoAsiento("ESTANDAR")
                        .estado("DISPONIBLE")
                        .build();
                asiento = viajeAsientoRepository.save(asiento);
            }

            if (!"DISPONIBLE".equals(asiento.getEstado())) {
                throw new RuntimeException("El asiento " + numeroAsiento + " no está disponible");
            }
        }

        // 5. Crear reserva directamente como PAGADO (venta presencial)
        String emailCliente = request.getClienteEmail() != null && !request.getClienteEmail().isEmpty()
                ? request.getClienteEmail()
                : request.getClienteCedula() + "@presencial.local";

        Reserva reserva = Reserva.builder()
                .viaje(viaje)
                .clienteEmail(emailCliente)
                .asientos(request.getAsientos().size())
                .estado("PAGADO")
                .monto(request.getPrecioTotal())
                .expiresAt(null) // No expira porque ya está pagada
                .build();

        reserva = reservaRepository.save(reserva);

        // 5.1 Marcar asientos como VENDIDOS y asociar con la reserva
        for (String numeroAsiento : request.getAsientos()) {
            ViajeAsiento asiento = viajeAsientoRepository
                    .findByViajeIdAndNumeroAsiento(viaje.getId(), numeroAsiento)
                    .orElseThrow();
            asiento.setEstado("VENDIDO");
            asiento.setReserva(reserva);
            viajeAsientoRepository.save(asiento);
        }

        log.info("Venta presencial creada: Reserva {} para viaje {} (frecuencia {}) en fecha {}. Cliente: {} {} ({})",
                reserva.getId(), viaje.getId(), frecuencia.getId(), request.getFecha(),
                request.getClienteNombres(), request.getClienteApellidos(), request.getClienteCedula());

        // 6. Construir respuesta
        return VentaPresencialResponse.builder()
                .reservaId(reserva.getId())
                .viajeId(viaje.getId())
                .asientos(request.getAsientos())
                .clienteNombres(request.getClienteNombres())
                .clienteApellidos(request.getClienteApellidos())
                .clienteCedula(request.getClienteCedula())
                .totalPagado(request.getPrecioTotal())
                .metodoPago(request.getMetodoPago())
                .estado("PAGADO")
                .mensaje("Venta realizada exitosamente")
                .build();
    }

    /**
     * Obtiene un viaje existente o crea uno nuevo para una frecuencia+fecha
     */
    private Viaje obtenerOCrearViaje(FrecuenciaViaje frecuenciaViaje, LocalDate fecha) {
        Bus bus = frecuenciaViaje.getBus();
        
        // Obtener o crear la frecuencia de catálogos correspondiente
        Frecuencia frecuenciaCatalogo = obtenerOCrearFrecuenciaCatalogo(frecuenciaViaje);
        
        // Buscar si ya existe un viaje para esta frecuencia y fecha
        List<Viaje> viajesExistentes = viajeRepository.findByFrecuenciaIdAndFecha(frecuenciaCatalogo.getId(), fecha);
        
        if (!viajesExistentes.isEmpty()) {
            log.debug("Viaje existente encontrado: ID={}", viajesExistentes.get(0).getId());
            return viajesExistentes.get(0);
        }

        // Crear nuevo viaje
        LocalTime horaSalida = frecuenciaViaje.getHoraSalida() != null 
                ? frecuenciaViaje.getHoraSalida() 
                : LocalTime.of(8, 0);

        Viaje nuevoViaje = Viaje.builder()
                .frecuencia(frecuenciaCatalogo)
                .bus(bus)
                .chofer(frecuenciaViaje.getChofer()) // Asignar chofer de la frecuencia
                .fecha(fecha)
                .horaSalida(horaSalida)
                .horaSalidaProgramada(horaSalida)
                .estado("PROGRAMADO")
                .build();

        nuevoViaje = viajeRepository.save(nuevoViaje);
        log.info("Viaje creado automáticamente: ID={}, Frecuencia={}, Fecha={}, Bus={}", 
                nuevoViaje.getId(), frecuenciaCatalogo.getId(), fecha, bus.getPlaca());

        // Inicializar asientos del viaje desde el layout del bus
        try {
            viajeAsientoService.inicializarAsientosViaje(nuevoViaje.getId());
            log.info("Asientos inicializados para viaje {}", nuevoViaje.getId());
        } catch (Exception e) {
            log.warn("No se pudieron inicializar asientos para viaje {}: {}", nuevoViaje.getId(), e.getMessage());
            // No lanzar excepción, el viaje puede funcionar sin configuración de asientos
        }

        return nuevoViaje;
    }

    /**
     * Obtiene o crea una Frecuencia de catálogo desde una FrecuenciaViaje
     */
    private Frecuencia obtenerOCrearFrecuenciaCatalogo(FrecuenciaViaje frecuenciaViaje) {
        // Buscar si ya existe una frecuencia de catálogo con los mismos datos
        String origen = frecuenciaViaje.getRuta().getOrigen();
        String destino = frecuenciaViaje.getRuta().getDestino();
        LocalTime horaSalida = frecuenciaViaje.getHoraSalida();
        
        // Buscar frecuencia existente
        List<Frecuencia> frecuenciasExistentes = frecuenciaRepository.findAll().stream()
                .filter(f -> f.getCooperativa().getId().equals(frecuenciaViaje.getBus().getCooperativa().getId())
                        && f.getOrigen().equals(origen)
                        && f.getDestino().equals(destino)
                        && f.getHoraSalida().equals(horaSalida))
                .toList();
        
        if (!frecuenciasExistentes.isEmpty()) {
            return frecuenciasExistentes.get(0);
        }
        
        // Crear nueva frecuencia de catálogo
        String diasOperacion = frecuenciaViaje.getDiasOperacion();
        // Truncar diasOperacion si es muy largo (máximo 32 caracteres)
        if (diasOperacion != null && diasOperacion.length() > 32) {
            diasOperacion = diasOperacion.substring(0, 32);
        }
        
        Frecuencia nuevaFrecuencia = Frecuencia.builder()
                .cooperativa(frecuenciaViaje.getBus().getCooperativa())
                .origen(origen)
                .destino(destino)
                .horaSalida(horaSalida)
                .diasOperacion(diasOperacion)
                .activa(true)
                .build();
        
        return frecuenciaRepository.save(nuevaFrecuencia);
    }
}
