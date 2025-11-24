package com.andinobus.backendsmartcode.operacion.application.services;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Frecuencia;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.FrecuenciaRepository;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.UsuarioCooperativa;
import com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories.UsuarioCooperativaRepository;
import com.andinobus.backendsmartcode.operacion.api.dto.ViajeChoferDtos.*;
import com.andinobus.backendsmartcode.operacion.domain.entities.CalificacionViaje;
import com.andinobus.backendsmartcode.operacion.domain.entities.Viaje;
import com.andinobus.backendsmartcode.operacion.domain.entities.ViajeAsiento;
import com.andinobus.backendsmartcode.operacion.domain.repositories.CalificacionViajeRepository;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeAsientoRepository;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeRepository;
import com.andinobus.backendsmartcode.common.errors.NotFoundException;
import com.andinobus.backendsmartcode.ventas.domain.entities.Reserva;
import com.andinobus.backendsmartcode.ventas.domain.repositories.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Profile("dev")
@Service
@RequiredArgsConstructor
public class ViajeChoferService {

    private final ViajeRepository viajeRepository;
    private final ViajeAsientoRepository viajeAsientoRepository;
    private final ReservaRepository reservaRepository;
    private final CalificacionViajeRepository calificacionViajeRepository;
    private final UsuarioCooperativaRepository usuarioCooperativaRepository;
    private final FrecuenciaRepository frecuenciaRepository;

    /**
     * Obtiene el viaje del día del chofer con lista de pasajeros
     */
    @Transactional(readOnly = true)
    public ViajeChoferResponse getViajeDelDia(Long choferId, LocalDate fecha) {
        List<Viaje> viajes = viajeRepository.findByChoferIdAndFechaAndEstadoActivo(choferId, fecha);
        
        if (viajes.isEmpty()) {
            return null; // No tiene viajes para esta fecha
        }

        // Tomar el primer viaje (asumiendo un viaje por día)
        Viaje viaje = viajes.get(0);
        
        return buildViajeChoferResponse(viaje);
    }

    /**
     * Inicia un viaje (cambia estado a EN_RUTA y registra hora de salida real)
     */
    @Transactional
    public ViajeOperacionResponse iniciarViaje(Long viajeId, IniciarViajeRequest request) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));

        if (!"PROGRAMADO".equals(viaje.getEstado())) {
            throw new RuntimeException("El viaje no está en estado PROGRAMADO. Estado actual: " + viaje.getEstado());
        }

        viaje.setEstado("EN_RUTA");
        viaje.setHoraSalidaReal(request.getHoraSalidaReal() != null ? request.getHoraSalidaReal() : LocalTime.now());
        viajeRepository.save(viaje);

        log.info("Viaje {} iniciado. Estado: EN_RUTA, Hora salida real: {}", 
                viajeId, viaje.getHoraSalidaReal());

        return ViajeOperacionResponse.builder()
                .viajeId(viajeId)
                .estado("EN_RUTA")
                .mensaje("Viaje iniciado exitosamente")
                .build();
    }

    /**
     * Finaliza un viaje (cambia estado a COMPLETADO y registra hora de llegada real)
     */
    @Transactional
    public ViajeOperacionResponse finalizarViaje(Long viajeId, FinalizarViajeRequest request) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));

        if (!"EN_RUTA".equals(viaje.getEstado())) {
            throw new RuntimeException("El viaje no está en estado EN_RUTA. Estado actual: " + viaje.getEstado());
        }

        viaje.setEstado("COMPLETADO");
        viaje.setHoraLlegadaReal(request.getHoraLlegadaReal() != null ? request.getHoraLlegadaReal() : LocalTime.now());
        
        if (request.getObservaciones() != null && !request.getObservaciones().isEmpty()) {
            viaje.setObservaciones(request.getObservaciones());
        }
        
        viajeRepository.save(viaje);

        log.info("Viaje {} finalizado. Estado: COMPLETADO, Hora llegada real: {}", 
                viajeId, viaje.getHoraLlegadaReal());

        return ViajeOperacionResponse.builder()
                .viajeId(viajeId)
                .estado("COMPLETADO")
                .mensaje("Viaje finalizado exitosamente")
                .build();
    }

    /**
     * Obtiene el historial de viajes completados del chofer
     */
    @Transactional(readOnly = true)
    public List<ViajeHistorialResponse> getHistorialViajes(Long choferId) {
        List<Viaje> viajes = viajeRepository.findViajesCompletadosByChoferId(choferId);
        
        return viajes.stream()
                .map(this::buildViajeHistorialResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el historial de viajes con filtro de fechas
     */
    @Transactional(readOnly = true)
    public List<ViajeHistorialResponse> getHistorialViajesByFechas(Long choferId, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Viaje> viajes = viajeRepository.findViajesCompletadosByChoferIdAndFechaBetween(choferId, fechaInicio, fechaFin);
        
        return viajes.stream()
                .map(this::buildViajeHistorialResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las calificaciones de un viaje específico
     */
    @Transactional(readOnly = true)
    public List<CalificacionResponse> getCalificacionesViaje(Long viajeId) {
        List<CalificacionViaje> calificaciones = calificacionViajeRepository.findByViajeIdAndActivaTrue(viajeId);
        
        return calificaciones.stream()
                .map(c -> CalificacionResponse.builder()
                        .id(c.getId())
                        .viajeId(c.getViaje().getId())
                        .clienteEmail(c.getClienteEmail())
                        .puntuacion(c.getPuntuacion())
                        .comentario(c.getComentario())
                        .fechaCalificacion(c.getFechaCalificacion())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las calificaciones del chofer
     */
    @Transactional(readOnly = true)
    public CalificacionesChoferResponse getCalificacionesChofer(Long choferId) {
        List<CalificacionViaje> calificaciones = calificacionViajeRepository.findByChoferIdOrderByFechaDesc(choferId);
        Double promedioCalificacion = calificacionViajeRepository.findAverageRatingByChoferId(choferId);
        
        List<CalificacionResponse> calificacionesResponse = calificaciones.stream()
                .map(c -> CalificacionResponse.builder()
                        .id(c.getId())
                        .viajeId(c.getViaje().getId())
                        .clienteEmail(c.getClienteEmail())
                        .puntuacion(c.getPuntuacion())
                        .comentario(c.getComentario())
                        .fechaCalificacion(c.getFechaCalificacion())
                        .origen(c.getViaje().getFrecuencia().getOrigen())
                        .destino(c.getViaje().getFrecuencia().getDestino())
                        .fechaViaje(c.getViaje().getFecha())
                        .build())
                .collect(Collectors.toList());
        
        return CalificacionesChoferResponse.builder()
                .calificaciones(calificacionesResponse)
                .promedioCalificacion(promedioCalificacion != null ? promedioCalificacion : 0.0)
                .totalCalificaciones(calificaciones.size())
                .build();
    }

    /**
     * Construye el DTO de respuesta con información del viaje y pasajeros
     */
    private ViajeChoferResponse buildViajeChoferResponse(Viaje viaje) {
        // Obtener todas las reservas del viaje (PAGADO y PENDIENTE activas)
        List<Reserva> reservas = reservaRepository.findActiveByViajeId(viaje.getId());
        
        // Obtener asientos del viaje
        List<ViajeAsiento> asientos = viajeAsientoRepository.findByViajeId(viaje.getId());
        
        // Agrupar asientos por reserva
        Map<Long, List<String>> asientosPorReserva = asientos.stream()
                .filter(a -> a.getReserva() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getReserva().getId(),
                        Collectors.mapping(ViajeAsiento::getNumeroAsiento, Collectors.toList())
                ));
        
        // Construir lista de pasajeros
        List<PasajeroViaje> pasajeros = reservas.stream()
                .map(reserva -> PasajeroViaje.builder()
                        .reservaId(reserva.getId())
                        .clienteEmail(reserva.getClienteEmail())
                        .asientos(asientosPorReserva.getOrDefault(reserva.getId(), List.of()))
                        .estado(reserva.getEstado())
                        .verificado(false) // TODO: Implementar verificación persistente
                        .build())
                .collect(Collectors.toList());
        
        return ViajeChoferResponse.builder()
                .id(viaje.getId())
                .origen(viaje.getFrecuencia().getOrigen())
                .destino(viaje.getFrecuencia().getDestino())
                .fecha(viaje.getFecha())
                .horaSalidaProgramada(viaje.getHoraSalidaProgramada())
                .horaSalidaReal(viaje.getHoraSalidaReal())
                .horaLlegadaEstimada(viaje.getHoraLlegadaEstimada())
                .horaLlegadaReal(viaje.getHoraLlegadaReal())
                .busPlaca(viaje.getBus().getPlaca())
                .busMarca(viaje.getBus().getChasisMarca())
                .capacidadTotal(viaje.getBus().getCapacidadAsientos())
                .estado(viaje.getEstado())
                .pasajeros(pasajeros)
                .totalPasajeros(pasajeros.size())
                .pasajerosVerificados(0) // TODO: Implementar contador real
                .build();
    }

    /**
     * Construye el DTO de respuesta para historial de viajes
     */
    private ViajeHistorialResponse buildViajeHistorialResponse(Viaje viaje) {
        // Contar pasajeros
        List<Reserva> reservas = reservaRepository.findActiveByViajeId(viaje.getId());
        
        // Obtener calificaciones del viaje
        List<CalificacionViaje> calificaciones = calificacionViajeRepository.findByViajeIdAndActivaTrue(viaje.getId());
        Double promedioCalificacion = calificaciones.isEmpty() ? null :
                calificaciones.stream()
                        .mapToInt(CalificacionViaje::getPuntuacion)
                        .average()
                        .orElse(0.0);
        
        return ViajeHistorialResponse.builder()
                .id(viaje.getId())
                .origen(viaje.getFrecuencia().getOrigen())
                .destino(viaje.getFrecuencia().getDestino())
                .fecha(viaje.getFecha())
                .horaSalidaProgramada(viaje.getHoraSalidaProgramada())
                .horaSalidaReal(viaje.getHoraSalidaReal())
                .horaLlegadaEstimada(viaje.getHoraLlegadaEstimada())
                .horaLlegadaReal(viaje.getHoraLlegadaReal())
                .busPlaca(viaje.getBus().getPlaca())
                .totalPasajeros(reservas.size())
                .promedioCalificacion(promedioCalificacion)
                .totalCalificaciones(calificaciones.size())
                .observaciones(viaje.getObservaciones())
                .build();
    }

    /**
     * Obtiene las rutas (frecuencias) asignadas a la cooperativa del chofer
     */
    @Transactional(readOnly = true)
    public List<RutaChoferResponse> getMisRutas(Long choferId) {
        // Obtener el chofer
        UsuarioCooperativa chofer = usuarioCooperativaRepository.findById(choferId)
                .orElseThrow(() -> new NotFoundException("Chofer no encontrado"));
        
        // Obtener la cooperativa del chofer
        Long cooperativaId = chofer.getCooperativa().getId();
        
        // Obtener todas las frecuencias activas de la cooperativa
        List<Frecuencia> frecuencias = frecuenciaRepository.findByCooperativa_IdAndActivaTrue(cooperativaId, null)
                .getContent();
        
        // Obtener todos los viajes completados del chofer
        List<Viaje> viajesCompletados = viajeRepository.findViajesCompletadosByChoferId(choferId);
        
        // Contar viajes por frecuencia
        Map<Long, Long> viajesPorFrecuencia = viajesCompletados.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getFrecuencia().getId(),
                        Collectors.counting()
                ));
        
        // Construir respuesta
        return frecuencias.stream()
                .map(frecuencia -> RutaChoferResponse.builder()
                        .id(frecuencia.getId())
                        .origen(frecuencia.getOrigen())
                        .destino(frecuencia.getDestino())
                        .horaSalida(frecuencia.getHoraSalida())
                        .duracionEstimadaMin(frecuencia.getDuracionEstimadaMin())
                        .diasOperacion(frecuencia.getDiasOperacion())
                        .activa(frecuencia.getActiva())
                        .totalViajesRealizados(viajesPorFrecuencia.getOrDefault(frecuencia.getId(), 0L).intValue())
                        .build())
                .collect(Collectors.toList());
    }
}
