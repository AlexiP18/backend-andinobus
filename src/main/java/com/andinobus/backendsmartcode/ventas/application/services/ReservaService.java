package com.andinobus.backendsmartcode.ventas.application.services;

import com.andinobus.backendsmartcode.admin.domain.repositories.FrecuenciaViajeRepository;
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
        List<ViajeAsiento> asientos = viajeAsientoRepository.findByViajeId(viajeId);
        
        // Obtener el layout del bus para mapear número de asiento a fila/columna
        if (asientos.isEmpty()) {
            return List.of();
        }
        
        Long busId = asientos.get(0).getViaje().getBus().getId();
        List<com.andinobus.backendsmartcode.catalogos.domain.entities.AsientoLayout> layoutAsientos = 
            asientoLayoutRepository.findByBusIdOrderByNumeroAsientoAsc(busId);
        
        // Crear un mapa para acceso rápido: numeroAsiento -> AsientoLayout
        var layoutMap = layoutAsientos.stream()
            .collect(Collectors.toMap(
                a -> String.valueOf(a.getNumeroAsiento()),
                a -> a
            ));
        
        return asientos.stream()
                .map(asiento -> {
                    var layout = layoutMap.get(asiento.getNumeroAsiento());
                    return VentasDtos.AsientoDisponibilidadDto.builder()
                            .numeroAsiento(asiento.getNumeroAsiento())
                            .tipoAsiento(asiento.getTipoAsiento())
                            .estado(asiento.getEstado())
                            .fila(layout != null ? layout.getFila() : null)
                            .columna(layout != null ? layout.getColumna() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VentasDtos.AsientoDisponibilidadDto> obtenerAsientosDisponiblesPorFrecuencia(Long frecuenciaViajeId, String fechaStr) {
        LocalDate fecha = LocalDate.parse(fechaStr);
        
        // Obtener la FrecuenciaViaje para saber qué bus usar
        com.andinobus.backendsmartcode.admin.domain.entities.FrecuenciaViaje frecuenciaViaje = 
            frecuenciaViajeRepository.findById(frecuenciaViajeId)
                .orElseThrow(() -> new RuntimeException("FrecuenciaViaje no encontrada"));
        
        Long busId = frecuenciaViaje.getBus().getId();
        
        // Buscar viajes para este bus y fecha
        List<Viaje> viajes = viajeRepository.findByBusIdAndFecha(busId, fecha);
        
        if (viajes.isEmpty()) {
            // No hay viaje creado aún, mostrar todos los asientos disponibles desde el layout
            List<com.andinobus.backendsmartcode.catalogos.domain.entities.AsientoLayout> layoutAsientos = 
                asientoLayoutRepository.findByBusIdOrderByNumeroAsientoAsc(busId);
            
            return layoutAsientos.stream()
                .filter(asiento -> asiento.getHabilitado())
                .map(asiento -> VentasDtos.AsientoDisponibilidadDto.builder()
                        .numeroAsiento(String.valueOf(asiento.getNumeroAsiento()))
                        .tipoAsiento(asiento.getTipoAsiento())
                        .estado("DISPONIBLE")
                        .fila(asiento.getFila())
                        .columna(asiento.getColumna())
                        .build())
                .collect(Collectors.toList());
        }
        
        // Si hay viaje(s), obtener asientos del primero
        Long viajeId = viajes.get(0).getId();
        return obtenerAsientosDisponibles(viajeId);
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
