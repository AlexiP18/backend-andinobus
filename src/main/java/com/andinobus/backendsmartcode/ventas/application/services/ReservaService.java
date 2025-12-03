package com.andinobus.backendsmartcode.ventas.application.services;

import com.andinobus.backendsmartcode.admin.domain.repositories.FrecuenciaViajeRepository;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Frecuencia;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.FrecuenciaRepository;
import com.andinobus.backendsmartcode.operacion.domain.entities.Viaje;
import com.andinobus.backendsmartcode.operacion.domain.entities.ViajeAsiento;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeAsientoRepository;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeRepository;
import com.andinobus.backendsmartcode.ventas.api.dto.VentasDtos;
import com.andinobus.backendsmartcode.ventas.domain.entities.Reserva;
import com.andinobus.backendsmartcode.ventas.domain.repositories.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.andinobus.backendsmartcode.common.errors.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Profile("dev")
@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ViajeRepository viajeRepository;
    private final ViajeAsientoRepository viajeAsientoRepository;
    private final FrecuenciaViajeRepository frecuenciaViajeRepository;
    private final FrecuenciaRepository frecuenciaRepository;
    private final com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.AsientoLayoutRepository asientoLayoutRepository;

    private static final int EXPIRATION_MINUTES = 15;
    private static final BigDecimal PRECIO_BASE = new BigDecimal("25.00");

    @Transactional
    public VentasDtos.ReservaResponse crearReserva(VentasDtos.ReservaCreateRequest request, String clienteEmail) {
        // 1. Validar que el viaje existe
        Viaje viaje = viajeRepository.findById(request.getViajeId())
                .orElseThrow(() -> new NotFoundException("Viaje no encontrado"));

        if (!"PROGRAMADO".equals(viaje.getEstado())) {
            throw new RuntimeException("El viaje no está disponible para reservas");
        }

        // 2. Validar y reservar asientos
        List<String> numeroAsientos = request.getAsientos();

        List<ViajeAsiento> asientosReservados = new ArrayList<>();
        for (String numeroAsiento : numeroAsientos) {
            ViajeAsiento asiento = viajeAsientoRepository
                    .findByViajeIdAndNumeroAsiento(viaje.getId(), numeroAsiento)
                    .orElseThrow(() -> new NotFoundException("Asiento " + numeroAsiento + " no encontrado"));

            if (!"DISPONIBLE".equals(asiento.getEstado())) {
                // Liberar asientos ya reservados en esta transacción
                asientosReservados.forEach(a -> {
                    a.setEstado("DISPONIBLE");
                    a.setReserva(null);
                });
                throw new RuntimeException("Asiento " + numeroAsiento + " no está disponible");
            }

            asientosReservados.add(asiento);
        }

        // 3. Crear reserva
        BigDecimal monto = PRECIO_BASE.multiply(new BigDecimal(numeroAsientos.size()));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

        Reserva reserva = Reserva.builder()
                .viaje(viaje)
                .clienteEmail(clienteEmail)
                .asientos(numeroAsientos.size())
                .estado("PENDIENTE")
                .monto(monto)
                .expiresAt(expiresAt)
                .build();

        reserva = reservaRepository.save(reserva);

        // 4. Actualizar asientos
        for (ViajeAsiento asiento : asientosReservados) {
            asiento.setEstado("RESERVADO");
            asiento.setReserva(reserva);
            viajeAsientoRepository.save(asiento);
        }

        log.info("Reserva creada: {} para viaje {} con {} asientos", reserva.getId(), viaje.getId(), numeroAsientos.size());

        return VentasDtos.ReservaResponse.builder()
                .id(reserva.getId())
                .viajeId(viaje.getId())
                .asientos(request.getAsientos())
                .estado(reserva.getEstado())
                .fechaExpira(expiresAt.toString())
                .build();
    }

    @Transactional(readOnly = true)
    public VentasDtos.ReservaDetalleResponse obtenerReserva(Long reservaId, String clienteEmail) {
        Reserva reserva;
        if (clienteEmail != null && !clienteEmail.isEmpty()) {
            reserva = reservaRepository.findByIdAndClienteEmail(reservaId, clienteEmail)
                    .orElseThrow(() -> new NotFoundException("Reserva no encontrada"));
        } else {
            reserva = reservaRepository.findById(reservaId)
                    .orElseThrow(() -> new NotFoundException("Reserva no encontrada"));
        }

        List<ViajeAsiento> asientos = viajeAsientoRepository.findByReservaId(reservaId);
        List<String> asientosStr = asientos.stream()
                .map(ViajeAsiento::getNumeroAsiento)
                .collect(Collectors.toList());

        Viaje viaje = reserva.getViaje();
        String origen = viaje.getFrecuencia() != null
                ? viaje.getFrecuencia().getOrigen()
                : "N/A";
        String destino = viaje.getFrecuencia() != null
                ? viaje.getFrecuencia().getDestino()
                : "N/A";
        String cooperativaNombre = viaje.getBus() != null && viaje.getBus().getCooperativa() != null
                ? viaje.getBus().getCooperativa().getNombre()
                : "N/A";

        return VentasDtos.ReservaDetalleResponse.builder()
                .id(reserva.getId())
                .viajeId(viaje.getId())
                .cliente(reserva.getClienteEmail())
                .asientos(asientosStr)
                .estado(reserva.getEstado())
                .monto(reserva.getMonto())
                .fecha(viaje.getFecha() != null ? viaje.getFecha().toString() : null)
                .horaSalida(viaje.getHoraSalida() != null ? viaje.getHoraSalida().toString() : null)
                .origen(origen)
                .destino(destino)
                .busPlaca(viaje.getBus() != null ? viaje.getBus().getPlaca() : null)
                .cooperativaNombre(cooperativaNombre)
                .rutaNombre(origen + " - " + destino)
                .codigoBoleto("BOL-" + reserva.getId())
                .build();
    }

    @Transactional(readOnly = true)
    public List<VentasDtos.ReservaDetalleResponse> listarReservasPorCliente(String clienteEmail) {
        List<Reserva> reservas = reservaRepository.findByClienteEmail(clienteEmail);
        return reservas.stream()
                .map(reserva -> {
                    List<ViajeAsiento> asientos = viajeAsientoRepository.findByReservaId(reserva.getId());
                    List<String> asientosStr = asientos.stream()
                            .map(ViajeAsiento::getNumeroAsiento)
                            .collect(Collectors.toList());
                    
                    Viaje viaje = reserva.getViaje();
                    String origen = viaje.getFrecuencia() != null
                            ? viaje.getFrecuencia().getOrigen()
                            : "N/A";
                    String destino = viaje.getFrecuencia() != null
                            ? viaje.getFrecuencia().getDestino()
                            : "N/A";
                    String cooperativaNombre = viaje.getBus() != null && viaje.getBus().getCooperativa() != null
                            ? viaje.getBus().getCooperativa().getNombre()
                            : "N/A";
                    
                    return VentasDtos.ReservaDetalleResponse.builder()
                            .id(reserva.getId())
                            .viajeId(viaje.getId())
                            .cliente(reserva.getClienteEmail())
                            .asientos(asientosStr)
                            .estado(reserva.getEstado())
                            .monto(reserva.getMonto())
                            .fecha(viaje.getFecha() != null ? viaje.getFecha().toString() : null)
                            .horaSalida(viaje.getHoraSalida() != null ? viaje.getHoraSalida().toString() : null)
                            .origen(origen)
                            .destino(destino)
                            .busPlaca(viaje.getBus() != null ? viaje.getBus().getPlaca() : null)
                            .cooperativaNombre(cooperativaNombre)
                            .rutaNombre(origen + " - " + destino)
                            .codigoBoleto("BOL-" + reserva.getId())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelarReserva(Long reservaId, String clienteEmail) {
        Reserva reserva;
        if (clienteEmail != null && !clienteEmail.isEmpty()) {
            reserva = reservaRepository.findByIdAndClienteEmail(reservaId, clienteEmail)
                    .orElseThrow(() -> new NotFoundException("Reserva no encontrada"));
        } else {
            reserva = reservaRepository.findById(reservaId)
                    .orElseThrow(() -> new NotFoundException("Reserva no encontrada"));
        }

        if ("PAGADO".equals(reserva.getEstado())) {
            throw new RuntimeException("No se puede cancelar una reserva pagada");
        }

        reserva.setEstado("CANCELADO");
        reservaRepository.save(reserva);

        // Liberar asientos
        viajeAsientoRepository.liberarAsientosPorReserva(reservaId);

        log.info("Reserva {} cancelada", reservaId);
    }

    @Transactional
    public void expirarReservasPendientes() {
        List<Reserva> reservasExpiradas = reservaRepository.findExpiredReservations(LocalDateTime.now());
        
        for (Reserva reserva : reservasExpiradas) {
            reserva.setEstado("EXPIRADO");
            reservaRepository.save(reserva);
            viajeAsientoRepository.liberarAsientosPorReserva(reserva.getId());
            log.info("Reserva {} expirada automáticamente", reserva.getId());
        }
    }

    @Transactional(readOnly = true)
    public List<VentasDtos.AsientoDisponibilidadDto> obtenerAsientosDisponibles(Long viajeId) {
        // Obtener el viaje para acceder al bus
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new NotFoundException("Viaje no encontrado"));
        
        Long busId = viaje.getBus().getId();
        
        // Obtener TODOS los asientos del layout del bus (habilitados y deshabilitados)
        List<com.andinobus.backendsmartcode.catalogos.domain.entities.AsientoLayout> layoutAsientos = 
            asientoLayoutRepository.findByBusIdOrderByNumeroAsientoAsc(busId);
        
        if (layoutAsientos.isEmpty()) {
            return List.of();
        }
        
        // Obtener los asientos del viaje (solo los habilitados tienen ViajeAsiento)
        List<ViajeAsiento> viajeAsientos = viajeAsientoRepository.findByViajeId(viajeId);
        
        // Crear un mapa para acceso rápido: numeroAsiento -> ViajeAsiento
        var viajeAsientoMap = viajeAsientos.stream()
            .collect(Collectors.toMap(
                ViajeAsiento::getNumeroAsiento,
                a -> a
            ));
        
        // Retornar TODOS los asientos del layout, marcando los deshabilitados como BLOQUEADO
        return layoutAsientos.stream()
                .map(layout -> {
                    String numeroAsiento = String.valueOf(layout.getNumeroAsiento());
                    ViajeAsiento viajeAsiento = viajeAsientoMap.get(numeroAsiento);
                    
                    String estado;
                    if (!layout.getHabilitado()) {
                        // Asiento deshabilitado en el layout
                        estado = "DESHABILITADO";
                    } else if (viajeAsiento != null) {
                        // Asiento tiene estado del viaje
                        estado = viajeAsiento.getEstado();
                    } else {
                        // Asiento habilitado pero sin ViajeAsiento (disponible por defecto)
                        estado = "DISPONIBLE";
                    }
                    
                    return VentasDtos.AsientoDisponibilidadDto.builder()
                            .numeroAsiento(numeroAsiento)
                            .tipoAsiento(layout.getTipoAsiento())
                            .estado(estado)
                            .fila(layout.getFila())
                            .columna(layout.getColumna())
                            .piso(layout.getPiso())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public VentasDtos.AsientosViajeResponse obtenerAsientosDisponiblesPorFrecuencia(Long frecuenciaViajeId, String fechaStr) {
        LocalDate fecha = LocalDate.parse(fechaStr);
        
        // Verificar que la frecuencia de viaje existe (tabla frecuencia_viaje)
        com.andinobus.backendsmartcode.admin.domain.entities.FrecuenciaViaje frecuenciaViaje = 
            frecuenciaViajeRepository.findById(frecuenciaViajeId)
                .orElseThrow(() -> new NotFoundException("Frecuencia no encontrada con id: " + frecuenciaViajeId));
        
        // Obtener o crear la frecuencia de catálogo correspondiente
        Frecuencia frecuenciaCatalogo = obtenerOCrearFrecuenciaCatalogo(frecuenciaViaje);
        
        // Buscar viajes para esta frecuencia de catálogo y fecha
        List<Viaje> viajes = viajeRepository.findByFrecuenciaIdAndFecha(frecuenciaCatalogo.getId(), fecha);
        
        Long viajeId;
        if (viajes.isEmpty()) {
            // Crear viaje automáticamente si no existe
            log.info("Creando viaje automático para frecuencia {} en fecha {}", frecuenciaViajeId, fechaStr);
            Viaje nuevoViaje = crearViajeAutomatico(frecuenciaViaje, frecuenciaCatalogo, fecha);
            viajeId = nuevoViaje.getId();
        } else {
            // Si hay viaje(s), usar el primero
            viajeId = viajes.get(0).getId();
        }
        
        List<VentasDtos.AsientoDisponibilidadDto> asientos = obtenerAsientosDisponibles(viajeId);
        
        return VentasDtos.AsientosViajeResponse.builder()
                .viajeId(viajeId)
                .asientos(asientos)
                .build();
    }
    
    /**
     * Crea un viaje automáticamente desde una FrecuenciaViaje
     */
    private Viaje crearViajeAutomatico(
            com.andinobus.backendsmartcode.admin.domain.entities.FrecuenciaViaje frecuenciaViaje,
            Frecuencia frecuenciaCatalogo,
            LocalDate fecha) {
        
        java.time.LocalTime horaSalida = frecuenciaViaje.getHoraSalida() != null 
                ? frecuenciaViaje.getHoraSalida() 
                : java.time.LocalTime.of(8, 0);

        Viaje nuevoViaje = Viaje.builder()
                .frecuencia(frecuenciaCatalogo)
                .bus(frecuenciaViaje.getBus())
                .fecha(fecha)
                .horaSalida(horaSalida)
                .horaSalidaProgramada(horaSalida)
                .estado("PROGRAMADO")
                .build();

        nuevoViaje = viajeRepository.save(nuevoViaje);
        log.info("Viaje creado automáticamente: ID={}, FrecuenciaViaje={}, Fecha={}", 
                nuevoViaje.getId(), frecuenciaViaje.getId(), fecha);

        // Inicializar asientos del viaje desde el layout del bus
        inicializarAsientosDesdeLayout(nuevoViaje);

        return nuevoViaje;
    }
    
    /**
     * Inicializa los asientos de un viaje desde el layout del bus
     */
    private void inicializarAsientosDesdeLayout(Viaje viaje) {
        try {
            Long busId = viaje.getBus().getId();
            List<com.andinobus.backendsmartcode.catalogos.domain.entities.AsientoLayout> layoutAsientos = 
                asientoLayoutRepository.findByBusIdOrderByNumeroAsientoAsc(busId);
            
            if (layoutAsientos.isEmpty()) {
                log.warn("Bus {} no tiene layout de asientos configurado, creando asientos por defecto", busId);
                // Crear asientos por defecto basados en la capacidad
                int capacidad = viaje.getBus().getCapacidadAsientos() != null 
                        ? viaje.getBus().getCapacidadAsientos() 
                        : 40;
                for (int i = 1; i <= capacidad; i++) {
                    ViajeAsiento asiento = ViajeAsiento.builder()
                            .viaje(viaje)
                            .numeroAsiento(String.valueOf(i))
                            .tipoAsiento("NORMAL")
                            .estado("DISPONIBLE")
                            .build();
                    viajeAsientoRepository.save(asiento);
                }
            } else {
                // Crear asientos desde el layout (solo los habilitados)
                for (var layout : layoutAsientos) {
                    if (layout.getHabilitado()) {
                        ViajeAsiento asiento = ViajeAsiento.builder()
                                .viaje(viaje)
                                .numeroAsiento(String.valueOf(layout.getNumeroAsiento()))
                                .tipoAsiento(layout.getTipoAsiento())
                                .estado("DISPONIBLE")
                                .build();
                        viajeAsientoRepository.save(asiento);
                    }
                }
            }
            log.info("Asientos inicializados para viaje {}", viaje.getId());
        } catch (Exception e) {
            log.error("Error inicializando asientos para viaje {}: {}", viaje.getId(), e.getMessage());
        }
    }
    
    /**
     * Obtiene o crea una Frecuencia de catálogo desde una FrecuenciaViaje
     */
    private Frecuencia obtenerOCrearFrecuenciaCatalogo(
            com.andinobus.backendsmartcode.admin.domain.entities.FrecuenciaViaje frecuenciaViaje) {
        
        String origen = frecuenciaViaje.getRuta().getOrigen();
        String destino = frecuenciaViaje.getRuta().getDestino();
        java.time.LocalTime horaSalida = frecuenciaViaje.getHoraSalida();
        Long cooperativaId = frecuenciaViaje.getBus().getCooperativa().getId();
        
        // Buscar frecuencia existente
        List<Frecuencia> frecuenciasExistentes = frecuenciaRepository.findAll().stream()
                .filter(f -> f.getCooperativa().getId().equals(cooperativaId)
                        && f.getOrigen().equals(origen)
                        && f.getDestino().equals(destino)
                        && f.getHoraSalida().equals(horaSalida))
                .toList();
        
        if (!frecuenciasExistentes.isEmpty()) {
            return frecuenciasExistentes.get(0);
        }
        
        // Crear nueva frecuencia de catálogo
        String diasOperacion = frecuenciaViaje.getDiasOperacion();
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
        
        Frecuencia saved = frecuenciaRepository.save(nuevaFrecuencia);
        log.info("Frecuencia de catálogo creada: ID={}, {}-{} {}", 
                saved.getId(), origen, destino, horaSalida);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<VentasDtos.ReservaCooperativaDto> obtenerReservasPorCooperativa(Long cooperativaId, String estadoFiltro) {
        // Obtener todas las reservas donde el viaje pertenece a un bus de esta cooperativa
        List<Reserva> reservas;
        
        if (estadoFiltro != null && !estadoFiltro.isEmpty()) {
            reservas = reservaRepository.findAll().stream()
                .filter(r -> r.getViaje() != null 
                    && r.getViaje().getBus() != null 
                    && r.getViaje().getBus().getCooperativa() != null
                    && r.getViaje().getBus().getCooperativa().getId().equals(cooperativaId)
                    && r.getEstado().equalsIgnoreCase(estadoFiltro))
                .collect(Collectors.toList());
        } else {
            reservas = reservaRepository.findAll().stream()
                .filter(r -> r.getViaje() != null 
                    && r.getViaje().getBus() != null 
                    && r.getViaje().getBus().getCooperativa() != null
                    && r.getViaje().getBus().getCooperativa().getId().equals(cooperativaId))
                .collect(Collectors.toList());
        }

        return reservas.stream()
            .map(this::mapToReservaCooperativaDto)
            .collect(Collectors.toList());
    }

    private VentasDtos.ReservaCooperativaDto mapToReservaCooperativaDto(Reserva reserva) {
        Viaje viaje = reserva.getViaje();
        
        // Obtener información de la ruta
        String origen = "N/A";
        String destino = "N/A";
        String rutaNombre = "N/A";
        
        if (viaje.getFrecuencia() != null) {
            origen = viaje.getFrecuencia().getOrigen();
            destino = viaje.getFrecuencia().getDestino();
            rutaNombre = origen + " - " + destino;
        }

        return VentasDtos.ReservaCooperativaDto.builder()
            .id(reserva.getId())
            .viajeId(viaje.getId())
            .clienteEmail(reserva.getClienteEmail())
            .asientos(reserva.getAsientos())
            .estado(reserva.getEstado())
            .monto(reserva.getMonto())
            .expiresAt(reserva.getExpiresAt() != null ? reserva.getExpiresAt().toString() : null)
            .createdAt(reserva.getCreatedAt() != null ? reserva.getCreatedAt().toString() : null)
            .fecha(viaje.getFecha() != null ? viaje.getFecha().toString() : null)
            .horaSalida(viaje.getHoraSalidaProgramada() != null ? viaje.getHoraSalidaProgramada().toString() : null)
            .origen(origen)
            .destino(destino)
            .busPlaca(viaje.getBus() != null ? viaje.getBus().getPlaca() : "N/A")
            .rutaNombre(rutaNombre)
            .build();
    }

}
