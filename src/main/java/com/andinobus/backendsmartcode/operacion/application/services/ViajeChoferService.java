package com.andinobus.backendsmartcode.operacion.application.services;

import com.andinobus.backendsmartcode.admin.domain.entities.FrecuenciaViaje;
import com.andinobus.backendsmartcode.admin.domain.repositories.FrecuenciaViajeRepository;
import com.andinobus.backendsmartcode.catalogos.domain.entities.BusChofer;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Frecuencia;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Terminal;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.TerminalRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusChoferRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViajeChoferService {

    private final ViajeRepository viajeRepository;
    private final ViajeAsientoRepository viajeAsientoRepository;
    private final ReservaRepository reservaRepository;
    private final CalificacionViajeRepository calificacionViajeRepository;
    private final UsuarioCooperativaRepository usuarioCooperativaRepository;
    private final FrecuenciaRepository frecuenciaRepository;
    private final FrecuenciaViajeRepository frecuenciaViajeRepository;
    private final TerminalRepository terminalRepository;
    private final NotificacionViajeService notificacionViajeService;
    private final BusChoferRepository busChoferRepository;

    /**
     * Obtiene el viaje del día del chofer con lista de pasajeros
     * Primero busca viajes directamente asignados al chofer,
     * si no encuentra, busca viajes del bus asignado al chofer,
     * si aún no encuentra, busca frecuencias programadas para hoy.
     * 
     * Filtra por hora: si ya pasó la hora de llegada estimada del viaje,
     * no lo muestra (busca el siguiente).
     */
    @Transactional
    public ViajeChoferResponse getViajeDelDia(Long choferId, LocalDate fecha) {
        log.info("Buscando viaje del día para chofer {} en fecha {}", choferId, fecha);
        
        LocalTime horaActual = LocalTime.now();
        boolean esFechaHoy = fecha.equals(LocalDate.now());
        
        // 1. Buscar viajes directamente asignados al chofer
        List<Viaje> viajes = viajeRepository.findByChoferIdAndFechaAndEstadoActivo(choferId, fecha);
        
        if (!viajes.isEmpty()) {
            // Ordenar por hora de salida y filtrar por hora si es hoy
            Viaje viajeDisponible = viajes.stream()
                .sorted((v1, v2) -> {
                    LocalTime h1 = v1.getHoraSalida() != null ? v1.getHoraSalida() : LocalTime.MIN;
                    LocalTime h2 = v2.getHoraSalida() != null ? v2.getHoraSalida() : LocalTime.MIN;
                    return h1.compareTo(h2);
                })
                .filter(v -> {
                    // Si está EN_RUTA, siempre mostrarlo
                    if ("EN_RUTA".equals(v.getEstado())) return true;
                    // Si no es hoy, no filtrar por hora
                    if (!esFechaHoy) return true;
                    // Si es hoy, verificar que no haya pasado la hora de llegada estimada
                    LocalTime horaLlegada = v.getHoraLlegadaEstimada();
                    if (horaLlegada == null) {
                        // Si no tiene hora de llegada, usar hora salida + 3 horas como estimado
                        horaLlegada = v.getHoraSalida() != null ? v.getHoraSalida().plusHours(3) : LocalTime.MAX;
                    }
                    return horaActual.isBefore(horaLlegada);
                })
                .findFirst()
                .orElse(null);
            
            if (viajeDisponible != null) {
                log.info("Viaje encontrado directamente asignado al chofer: viajeId={}", viajeDisponible.getId());
                return buildViajeChoferResponse(viajeDisponible);
            }
        }
        
        // 2. Si no hay viaje asignado directamente, buscar por buses asignados al chofer
        List<BusChofer> asignacionesBus = busChoferRepository.findByChoferIdAndActivoTrue(choferId);
        log.info("Chofer {} tiene {} buses asignados", choferId, asignacionesBus.size());
        
        if (!asignacionesBus.isEmpty()) {
            for (BusChofer asignacion : asignacionesBus) {
                Long busId = asignacion.getBus().getId();
                log.info("Buscando viajes para bus {} en fecha {}", busId, fecha);
                
                // Buscar viajes del bus para esta fecha
                List<Viaje> viajesDelBus = viajeRepository.findByBusIdAndFechaAndEstadoActivo(busId, fecha);
                log.info("Se encontraron {} viajes para el bus {}", viajesDelBus.size(), busId);
                
                if (!viajesDelBus.isEmpty()) {
                    // Filtrar por hora si es hoy
                    Viaje viajeDisponible = viajesDelBus.stream()
                        .sorted((v1, v2) -> {
                            LocalTime h1 = v1.getHoraSalida() != null ? v1.getHoraSalida() : LocalTime.MIN;
                            LocalTime h2 = v2.getHoraSalida() != null ? v2.getHoraSalida() : LocalTime.MIN;
                            return h1.compareTo(h2);
                        })
                        .filter(v -> {
                            if ("EN_RUTA".equals(v.getEstado())) return true;
                            if (!esFechaHoy) return true;
                            LocalTime horaLlegada = v.getHoraLlegadaEstimada();
                            if (horaLlegada == null) {
                                horaLlegada = v.getHoraSalida() != null ? v.getHoraSalida().plusHours(3) : LocalTime.MAX;
                            }
                            return horaActual.isBefore(horaLlegada);
                        })
                        .findFirst()
                        .orElse(null);
                    
                    if (viajeDisponible != null) {
                        // Asignar automáticamente el chofer al viaje encontrado
                        if (viajeDisponible.getChofer() == null) {
                            UsuarioCooperativa chofer = usuarioCooperativaRepository.findById(choferId).orElse(null);
                            if (chofer != null) {
                                viajeDisponible.setChofer(chofer);
                                viajeRepository.save(viajeDisponible);
                                log.info("Chofer {} asignado automáticamente al viaje {} del bus {}", 
                                        choferId, viajeDisponible.getId(), busId);
                            }
                        }
                        return buildViajeChoferResponse(viajeDisponible);
                    }
                }
            }
        }
        
        // 3. Si no hay viajes creados, buscar frecuencias programadas para el día
        FrecuenciaViaje frecuenciaHoy = buscarFrecuenciaParaHoy(choferId, fecha, asignacionesBus, horaActual, esFechaHoy);
        if (frecuenciaHoy != null) {
            log.debug("Frecuencia programada encontrada para chofer {} en fecha {}", choferId, fecha);
            return buildViajeFromFrecuencia(frecuenciaHoy, fecha);
        }
        
        log.debug("No se encontró viaje ni frecuencia para chofer {} en fecha {}", choferId, fecha);
        return null; // No tiene viajes para esta fecha
    }
    
    /**
     * Busca una frecuencia de viaje programada para el día indicado
     * Primero busca frecuencias directamente asignadas al chofer,
     * si no encuentra, busca frecuencias de los buses asignados al chofer.
     * Filtra por hora si es el día actual.
     */
    private FrecuenciaViaje buscarFrecuenciaParaHoy(Long choferId, LocalDate fecha, List<BusChofer> asignacionesBus, 
                                                     LocalTime horaActual, boolean esFechaHoy) {
        String diaSemana = obtenerDiaSemana(fecha);
        
        // 1. Buscar frecuencias directamente asignadas al chofer
        List<FrecuenciaViaje> frecuenciasChofer = frecuenciaViajeRepository.findByChoferIdAndActivoTrue(choferId);
        
        // Filtrar y ordenar por hora de salida
        FrecuenciaViaje frecuenciaDisponible = frecuenciasChofer.stream()
            .filter(f -> f.getDiasOperacion() != null && f.getDiasOperacion().toUpperCase().contains(diaSemana))
            .sorted((f1, f2) -> {
                LocalTime h1 = f1.getHoraSalida() != null ? f1.getHoraSalida() : LocalTime.MIN;
                LocalTime h2 = f2.getHoraSalida() != null ? f2.getHoraSalida() : LocalTime.MIN;
                return h1.compareTo(h2);
            })
            .filter(f -> {
                if (!esFechaHoy) return true;
                // Si es hoy, verificar que no haya pasado la hora de llegada estimada
                LocalTime horaSalida = f.getHoraSalida();
                Integer duracionMin = f.getDuracionEstimadaMinutos();
                LocalTime horaLlegada = (horaSalida != null && duracionMin != null) 
                    ? horaSalida.plusMinutes(duracionMin) 
                    : (horaSalida != null ? horaSalida.plusHours(3) : LocalTime.MAX);
                return horaActual.isBefore(horaLlegada);
            })
            .findFirst()
            .orElse(null);
        
        if (frecuenciaDisponible != null) {
            return frecuenciaDisponible;
        }
        
        // 2. Buscar frecuencias de los buses asignados al chofer
        if (asignacionesBus == null || asignacionesBus.isEmpty()) {
            asignacionesBus = busChoferRepository.findByChoferIdAndActivoTrue(choferId);
        }
        
        for (BusChofer asignacion : asignacionesBus) {
            Long busId = asignacion.getBus().getId();
            List<FrecuenciaViaje> frecuenciasBus = frecuenciaViajeRepository.findByBusIdOrderByHoraSalida(busId);
            
            frecuenciaDisponible = frecuenciasBus.stream()
                .filter(f -> f.getDiasOperacion() != null && f.getDiasOperacion().toUpperCase().contains(diaSemana))
                .sorted((f1, f2) -> {
                    LocalTime h1 = f1.getHoraSalida() != null ? f1.getHoraSalida() : LocalTime.MIN;
                    LocalTime h2 = f2.getHoraSalida() != null ? f2.getHoraSalida() : LocalTime.MIN;
                    return h1.compareTo(h2);
                })
                .filter(f -> {
                    if (!esFechaHoy) return true;
                    LocalTime horaSalida = f.getHoraSalida();
                    Integer duracionMin = f.getDuracionEstimadaMinutos();
                    LocalTime horaLlegada = (horaSalida != null && duracionMin != null) 
                        ? horaSalida.plusMinutes(duracionMin) 
                        : (horaSalida != null ? horaSalida.plusHours(3) : LocalTime.MAX);
                    return horaActual.isBefore(horaLlegada);
                })
                .findFirst()
                .orElse(null);
            
            if (frecuenciaDisponible != null) {
                return frecuenciaDisponible;
            }
        }
        
        return null;
    }
    
    /**
     * Convierte el día de la semana de LocalDate a formato de texto
     */
    private String obtenerDiaSemana(LocalDate fecha) {
        return switch (fecha.getDayOfWeek()) {
            case MONDAY -> "LUNES";
            case TUESDAY -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES";
            case FRIDAY -> "VIERNES";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };
    }
    
    /**
     * Construye un ViajeChoferResponse a partir de una FrecuenciaViaje
     * (para cuando no existe un viaje creado pero hay una frecuencia programada)
     */
    private ViajeChoferResponse buildViajeFromFrecuencia(FrecuenciaViaje frecuencia, LocalDate fecha) {
        // Extraer el cantón del nombre de la terminal (formato: "PROVINCIA|CANTON|ID")
        String origen = extraerCanton(frecuencia.getTerminalOrigen() != null 
            ? frecuencia.getTerminalOrigen().getNombre() 
            : (frecuencia.getRuta() != null ? frecuencia.getRuta().getOrigen() : ""));
        String destino = extraerCanton(frecuencia.getTerminalDestino() != null 
            ? frecuencia.getTerminalDestino().getNombre() 
            : (frecuencia.getRuta() != null ? frecuencia.getRuta().getDestino() : ""));
        
        // Construir coordenadas desde los terminales
        CoordenadaDTO coordenadaOrigen = buildCoordenadaFromTerminal(frecuencia.getTerminalOrigen());
        CoordenadaDTO coordenadaDestino = buildCoordenadaFromTerminal(frecuencia.getTerminalDestino());
        
        return ViajeChoferResponse.builder()
                .id(null) // No hay viaje creado aún
                .frecuenciaId(frecuencia.getId()) // Guardamos el ID de la frecuencia
                .origen(origen)
                .destino(destino)
                .fecha(fecha)
                .horaSalidaProgramada(frecuencia.getHoraSalida())
                .busPlaca(frecuencia.getBus() != null ? frecuencia.getBus().getPlaca() : null)
                .busMarca(frecuencia.getBus() != null ? frecuencia.getBus().getChasisMarca() : null)
                .capacidadTotal(frecuencia.getBus() != null ? frecuencia.getBus().getCapacidadAsientos() : null)
                .capacidadPiso1(frecuencia.getBus() != null ? frecuencia.getBus().getCapacidadPiso1() : null)
                .capacidadPiso2(frecuencia.getBus() != null ? frecuencia.getBus().getCapacidadPiso2() : null)
                .estado("PROGRAMADO") // Es un viaje programado (frecuencia)
                .pasajeros(List.of()) // Sin pasajeros aún
                .coordenadaOrigen(coordenadaOrigen)
                .coordenadaDestino(coordenadaDestino)
                .cooperativaId(frecuencia.getCooperativa() != null ? frecuencia.getCooperativa().getId() : null)
                .cooperativaNombre(frecuencia.getCooperativa() != null ? frecuencia.getCooperativa().getNombre() : null)
                .build();
    }
    
    /**
     * Construye un CoordenadaDTO a partir de un Terminal
     */
    private CoordenadaDTO buildCoordenadaFromTerminal(Terminal terminal) {
        if (terminal == null) {
            return null;
        }
        return CoordenadaDTO.builder()
                .latitud(terminal.getLatitud())
                .longitud(terminal.getLongitud())
                .nombreTerminal(terminal.getNombre())
                .canton(terminal.getCanton())
                .provincia(terminal.getProvincia())
                .build();
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

        // Validar que el viaje sea del día actual (no de días futuros)
        LocalDate fechaViaje = viaje.getFecha();
        LocalDate fechaHoy = LocalDate.now();
        
        if (fechaViaje != null && fechaViaje.isAfter(fechaHoy)) {
            throw new RuntimeException(
                String.format("No puede iniciar un viaje programado para otro día. El viaje está programado para %s",
                    fechaViaje.toString()));
        }

        // Validar que la hora actual sea igual o posterior a la hora programada de salida
        LocalTime horaActual = LocalTime.now();
        LocalTime horaProgramada = viaje.getHoraSalidaProgramada();
        
        if (horaProgramada != null && horaActual.isBefore(horaProgramada)) {
            // Permitir iniciar hasta 10 minutos antes de la hora programada
            LocalTime horaMinima = horaProgramada.minusMinutes(10);
            if (horaActual.isBefore(horaMinima)) {
                throw new RuntimeException(
                    String.format("No puede iniciar el viaje aún. Hora programada: %s. Puede iniciar a partir de las %s",
                        horaProgramada.toString(), horaMinima.toString()));
            }
        }

        viaje.setEstado("EN_RUTA");
        viaje.setHoraSalidaReal(request.getHoraSalidaReal() != null ? request.getHoraSalidaReal() : LocalTime.now());
        viajeRepository.save(viaje);

        // Notificar a la cooperativa
        try {
            notificacionViajeService.notificarViajeIniciado(viaje);
        } catch (Exception e) {
            log.error("Error al crear notificación de viaje iniciado: {}", e.getMessage());
        }

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

        // Notificar a la cooperativa
        try {
            notificacionViajeService.notificarViajeFinalizado(viaje, request.getObservaciones());
        } catch (Exception e) {
            log.error("Error al crear notificación de viaje finalizado: {}", e.getMessage());
        }

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
        log.info("Construyendo respuesta para viaje ID={}", viaje.getId());
        
        // Obtener todas las reservas del viaje (PAGADO y PENDIENTE activas)
        List<Reserva> reservas = reservaRepository.findActiveByViajeId(viaje.getId());
        log.info("Viaje {} tiene {} reservas activas", viaje.getId(), reservas.size());
        
        // Obtener asientos del viaje
        List<ViajeAsiento> asientos = viajeAsientoRepository.findByViajeId(viaje.getId());
        log.info("Viaje {} tiene {} asientos registrados", viaje.getId(), asientos.size());
        
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
        
        log.info("Viaje {} - Pasajeros construidos: {}", viaje.getId(), pasajeros.size());
        
        // Obtener coordenadas de los terminales buscando por nombre del origen/destino
        CoordenadaDTO coordOrigen = buscarCoordenadaTerminal(viaje.getFrecuencia().getOrigen());
        CoordenadaDTO coordDestino = buscarCoordenadaTerminal(viaje.getFrecuencia().getDestino());
        
        // Obtener información de la cooperativa
        Long cooperativaId = viaje.getBus().getCooperativa() != null ? 
                viaje.getBus().getCooperativa().getId() : null;
        String cooperativaNombre = viaje.getBus().getCooperativa() != null ? 
                viaje.getBus().getCooperativa().getNombre() : null;
        
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
                .capacidadPiso1(viaje.getBus().getCapacidadPiso1())
                .capacidadPiso2(viaje.getBus().getCapacidadPiso2())
                .estado(viaje.getEstado())
                .pasajeros(pasajeros)
                .totalPasajeros(pasajeros.size())
                .pasajerosVerificados(0) // TODO: Implementar contador real
                .coordenadaOrigen(coordOrigen)
                .coordenadaDestino(coordDestino)
                .cooperativaId(cooperativaId)
                .cooperativaNombre(cooperativaNombre)
                .build();
    }
    
    /**
     * Busca las coordenadas de un terminal por nombre o cantón
     * Soporta formato "PROVINCIA|CANTON|ID" extrayendo el cantón automáticamente
     */
    private CoordenadaDTO buscarCoordenadaTerminal(String nombreOrigen) {
        if (nombreOrigen == null || nombreOrigen.isEmpty()) {
            return null;
        }
        
        // Si tiene formato "PROVINCIA|CANTON|ID", extraer cantón
        String busqueda = nombreOrigen;
        if (nombreOrigen.contains("|")) {
            String[] partes = nombreOrigen.split("\\|");
            if (partes.length >= 2) {
                busqueda = partes[1]; // Usar el cantón para buscar
            }
        }
        
        // Intentar buscar primero por nombre exacto
        Optional<Terminal> terminalOpt = terminalRepository.findByNombreIgnoreCase(busqueda);
        
        // Si no se encuentra, buscar por cantón
        if (terminalOpt.isEmpty()) {
            List<Terminal> terminalesCanton = terminalRepository.findByCantonIgnoreCase(busqueda);
            if (!terminalesCanton.isEmpty()) {
                terminalOpt = Optional.of(terminalesCanton.get(0));
            }
        }
        
        // Si aún no se encuentra, buscar por texto parcial
        if (terminalOpt.isEmpty()) {
            List<Terminal> terminalesBusqueda = terminalRepository.buscarPorTexto(busqueda);
            if (!terminalesBusqueda.isEmpty()) {
                terminalOpt = Optional.of(terminalesBusqueda.get(0));
            }
        }
        
        return terminalOpt.map(terminal -> CoordenadaDTO.builder()
                .latitud(terminal.getLatitud())
                .longitud(terminal.getLongitud())
                .nombreTerminal(terminal.getNombre())
                .canton(terminal.getCanton())
                .provincia(terminal.getProvincia())
                .build())
                .orElse(null);
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
     * Obtiene las rutas (frecuencias de viaje) asignadas específicamente al chofer
     * Primero busca por chofer asignado en FrecuenciaViaje, luego por buses asignados
     */
    @Transactional(readOnly = true)
    public List<RutaChoferResponse> getMisRutas(Long choferId) {
        // Obtener el chofer
        UsuarioCooperativa chofer = usuarioCooperativaRepository.findById(choferId)
                .orElseThrow(() -> new NotFoundException("Chofer no encontrado"));
        
        log.debug("Buscando rutas para chofer: {} {} (id={})", chofer.getNombres(), chofer.getApellidos(), choferId);
        
        // 1. Primero buscar frecuencias donde el chofer está directamente asignado
        List<FrecuenciaViaje> frecuenciasChofer = frecuenciaViajeRepository.findByChoferIdAndActivoTrue(choferId);
        
        // 2. Si no hay frecuencias directas, buscar por buses asignados al chofer
        if (frecuenciasChofer.isEmpty()) {
            List<BusChofer> asignacionesBus = busChoferRepository.findByChoferIdAndActivoTrue(choferId);
            
            if (!asignacionesBus.isEmpty()) {
                List<Long> busIds = asignacionesBus.stream()
                        .map(bc -> bc.getBus().getId())
                        .collect(Collectors.toList());
                
                log.debug("Chofer tiene {} buses asignados: {}", busIds.size(), busIds);
                frecuenciasChofer = frecuenciaViajeRepository.findByBusIdInAndActivoTrue(busIds);
            }
        }
        
        log.debug("Se encontraron {} frecuencias para el chofer", frecuenciasChofer.size());
        
        // Obtener todos los viajes completados del chofer para estadísticas
        List<Viaje> viajesCompletados = viajeRepository.findViajesCompletadosByChoferId(choferId);
        
        // Contar viajes por frecuencia (usando frecuencia_id de Viaje)
        Map<Long, Long> viajesPorFrecuencia = viajesCompletados.stream()
                .filter(v -> v.getFrecuencia() != null)
                .collect(Collectors.groupingBy(
                        v -> v.getFrecuencia().getId(),
                        Collectors.counting()
                ));
        
        // Construir respuesta desde FrecuenciaViaje
        return frecuenciasChofer.stream()
                .map(fv -> {
                    // Obtener cantón de origen (prioridad: Terminal de FrecuenciaViaje > Terminal de Ruta > extraer de string)
                    String cantonOrigen;
                    String terminalOrigenNombre = null;
                    
                    if (fv.getTerminalOrigen() != null) {
                        cantonOrigen = fv.getTerminalOrigen().getCanton();
                        terminalOrigenNombre = fv.getTerminalOrigen().getNombre();
                    } else if (fv.getRuta() != null && fv.getRuta().getTerminalOrigen() != null) {
                        cantonOrigen = fv.getRuta().getTerminalOrigen().getCanton();
                        terminalOrigenNombre = fv.getRuta().getTerminalOrigen().getNombre();
                    } else if (fv.getRuta() != null) {
                        cantonOrigen = extraerCanton(fv.getRuta().getOrigen());
                    } else {
                        cantonOrigen = "No definido";
                    }
                    
                    // Obtener cantón de destino (prioridad: Terminal de FrecuenciaViaje > Terminal de Ruta > extraer de string)
                    String cantonDestino;
                    String terminalDestinoNombre = null;
                    
                    if (fv.getTerminalDestino() != null) {
                        cantonDestino = fv.getTerminalDestino().getCanton();
                        terminalDestinoNombre = fv.getTerminalDestino().getNombre();
                    } else if (fv.getRuta() != null && fv.getRuta().getTerminalDestino() != null) {
                        cantonDestino = fv.getRuta().getTerminalDestino().getCanton();
                        terminalDestinoNombre = fv.getRuta().getTerminalDestino().getNombre();
                    } else if (fv.getRuta() != null) {
                        cantonDestino = extraerCanton(fv.getRuta().getDestino());
                    } else {
                        cantonDestino = "No definido";
                    }
                    
                    Integer duracion = fv.getRuta() != null ? fv.getRuta().getDuracionEstimadaMinutos() : null;
                    Long frecuenciaId = fv.getId();
                    
                    return RutaChoferResponse.builder()
                            .id(fv.getId())
                            .origen(cantonOrigen)
                            .destino(cantonDestino)
                            .terminalOrigenNombre(terminalOrigenNombre)
                            .terminalDestinoNombre(terminalDestinoNombre)
                            .horaSalida(fv.getHoraSalida())
                            .duracionEstimadaMin(duracion)
                            .diasOperacion(fv.getDiasOperacion())
                            .activa(fv.getActivo())
                            .totalViajesRealizados(viajesPorFrecuencia.getOrDefault(frecuenciaId, 0L).intValue())
                            .busPlaca(fv.getBus() != null ? fv.getBus().getPlaca() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Extrae el cantón de un string con formato "PROVINCIA|CANTON|ID"
     * Si no tiene ese formato, retorna el string original
     */
    private String extraerCanton(String valor) {
        if (valor == null || valor.isEmpty()) {
            return "No definido";
        }
        if (valor.contains("|")) {
            String[] partes = valor.split("\\|");
            return partes.length > 1 ? partes[1] : partes[0];
        }
        return valor;
    }
}
