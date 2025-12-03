package com.andinobus.backendsmartcode.operacion.application.services;

import com.andinobus.backendsmartcode.admin.domain.entities.FrecuenciaViaje;
import com.andinobus.backendsmartcode.admin.domain.repositories.FrecuenciaViajeRepository;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Terminal;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.TerminalRepository;
import com.andinobus.backendsmartcode.catalogos.application.services.OcupacionTerminalService;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.UsuarioCooperativa;
import com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories.UsuarioCooperativaRepository;
import com.andinobus.backendsmartcode.operacion.domain.entities.Viaje;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Servicio para gestionar las asignaciones de frecuencias a choferes
 * y validar las restricciones de horas de trabajo.
 * 
 * Reglas de negocio:
 * - Chofer puede trabajar máximo 8 horas por día normalmente
 * - Máximo 2 días a la semana puede trabajar 10 horas
 * - Debe validarse la disponibilidad del terminal antes de asignar frecuencia
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsignacionFrecuenciaService {

    private final FrecuenciaViajeRepository frecuenciaViajeRepository;
    private final ViajeRepository viajeRepository;
    private final UsuarioCooperativaRepository usuarioCooperativaRepository;
    private final TerminalRepository terminalRepository;
    private final OcupacionTerminalService ocupacionTerminalService;

    // Constantes de reglas de negocio
    private static final int HORAS_TRABAJO_NORMAL = 8;
    private static final int HORAS_TRABAJO_MAXIMO = 10;
    private static final int DIAS_PERMITIDOS_10_HORAS = 2;

    /**
     * Valida si un chofer puede ser asignado a una frecuencia considerando sus horas de trabajo
     */
    @Transactional(readOnly = true)
    public ValidacionChoferResponse validarAsignacionChofer(
            Long choferId, 
            LocalDate fecha, 
            int duracionViajeMinutos) {
        
        UsuarioCooperativa chofer = usuarioCooperativaRepository.findById(choferId)
                .orElseThrow(() -> new RuntimeException("Chofer no encontrado: " + choferId));

        // Obtener horas trabajadas en el día
        int minutosTrabjadosHoy = calcularMinutosTrabajadosEnDia(choferId, fecha);
        int horasTrabajadasHoy = minutosTrabjadosHoy / 60;

        // Verificar si ya llegó al límite del día
        int nuevoTotalMinutos = minutosTrabjadosHoy + duracionViajeMinutos;
        int nuevoTotalHoras = nuevoTotalMinutos / 60;

        // Obtener días con jornada extendida esta semana
        int diasConJornadaExtendida = contarDiasJornadaExtendidaSemana(choferId, fecha);
        boolean yaUsoDias10Horas = diasConJornadaExtendida >= DIAS_PERMITIDOS_10_HORAS;

        // Determinar límite aplicable
        int limiteHorasHoy;
        if (yaUsoDias10Horas && !tienJornadaExtendidaHoy(choferId, fecha)) {
            limiteHorasHoy = HORAS_TRABAJO_NORMAL;
        } else {
            limiteHorasHoy = HORAS_TRABAJO_MAXIMO;
        }

        boolean puedeTrabajar = nuevoTotalHoras <= limiteHorasHoy;
        
        String mensaje;
        if (!puedeTrabajar) {
            if (yaUsoDias10Horas) {
                mensaje = String.format(
                    "El chofer ya ha utilizado sus %d días de jornada extendida esta semana. " +
                    "Límite hoy: %d horas. Horas actuales: %d. Con este viaje: %d horas.",
                    DIAS_PERMITIDOS_10_HORAS, limiteHorasHoy, horasTrabajadasHoy, nuevoTotalHoras);
            } else {
                mensaje = String.format(
                    "El chofer excedería el límite de %d horas. " +
                    "Horas actuales: %d. Con este viaje: %d horas.",
                    limiteHorasHoy, horasTrabajadasHoy, nuevoTotalHoras);
            }
        } else {
            boolean usaraJornadaExtendida = nuevoTotalHoras > HORAS_TRABAJO_NORMAL;
            if (usaraJornadaExtendida) {
                int diasRestantes = DIAS_PERMITIDOS_10_HORAS - diasConJornadaExtendida;
                mensaje = String.format(
                    "Asignación válida. Nota: Este día contará como jornada extendida. " +
                    "Días restantes de jornada extendida esta semana: %d", diasRestantes);
            } else {
                mensaje = "Asignación válida dentro de jornada normal.";
            }
        }

        return ValidacionChoferResponse.builder()
                .choferId(choferId)
                .choferNombre(chofer.getNombreCompleto())
                .fecha(fecha)
                .horasTrabajadasHoy(horasTrabajadasHoy)
                .minutosTrabajadasHoy(minutosTrabjadosHoy)
                .horasPropuestas(nuevoTotalHoras)
                .limiteHorasHoy(limiteHorasHoy)
                .diasJornadaExtendidaSemana(diasConJornadaExtendida)
                .puedeAsignarse(puedeTrabajar)
                .mensaje(mensaje)
                .build();
    }

    /**
     * Valida si una frecuencia puede asignarse a un terminal en una hora específica
     */
    @Transactional(readOnly = true)
    public ValidacionTerminalResponse validarAsignacionTerminal(
            Long terminalOrigenId,
            Long terminalDestinoId,
            LocalDate fecha,
            LocalTime horaSalida,
            LocalTime horaLlegada) {

        // Validar terminal de origen
        boolean origenDisponible = ocupacionTerminalService.tieneDisponibilidad(
                terminalOrigenId, fecha, horaSalida);

        Terminal terminalOrigen = terminalRepository.findById(terminalOrigenId)
                .orElseThrow(() -> new RuntimeException("Terminal origen no encontrado"));

        // Validar terminal de destino
        boolean destinoDisponible = ocupacionTerminalService.tieneDisponibilidad(
                terminalDestinoId, fecha, horaLlegada);

        Terminal terminalDestino = terminalRepository.findById(terminalDestinoId)
                .orElseThrow(() -> new RuntimeException("Terminal destino no encontrado"));

        boolean puedeAsignarse = origenDisponible && destinoDisponible;
        
        String mensaje;
        if (puedeAsignarse) {
            mensaje = "Ambos terminales tienen disponibilidad para la frecuencia.";
        } else {
            StringBuilder sb = new StringBuilder("No se puede asignar la frecuencia: ");
            if (!origenDisponible) {
                sb.append(String.format("Terminal origen (%s) saturado a las %s. ", 
                        terminalOrigen.getNombre(), horaSalida));
            }
            if (!destinoDisponible) {
                sb.append(String.format("Terminal destino (%s) saturado a las %s.", 
                        terminalDestino.getNombre(), horaLlegada));
            }
            mensaje = sb.toString();
        }

        return ValidacionTerminalResponse.builder()
                .terminalOrigenId(terminalOrigenId)
                .terminalOrigenNombre(terminalOrigen.getNombre())
                .terminalDestinoId(terminalDestinoId)
                .terminalDestinoNombre(terminalDestino.getNombre())
                .fecha(fecha)
                .horaSalida(horaSalida)
                .horaLlegada(horaLlegada)
                .origenDisponible(origenDisponible)
                .destinoDisponible(destinoDisponible)
                .puedeAsignarse(puedeAsignarse)
                .mensaje(mensaje)
                .build();
    }

    /**
     * Asigna una frecuencia validando todas las restricciones
     */
    @Transactional
    public AsignacionResponse asignarFrecuencia(AsignacionRequest request) {
        // 1. Validar chofer
        ValidacionChoferResponse validacionChofer = validarAsignacionChofer(
                request.getChoferId(),
                request.getFecha(),
                request.getDuracionMinutos());

        if (!validacionChofer.isPuedeAsignarse()) {
            return AsignacionResponse.builder()
                    .exitoso(false)
                    .mensaje("Error de chofer: " + validacionChofer.getMensaje())
                    .build();
        }

        // 2. Validar terminales
        ValidacionTerminalResponse validacionTerminal = validarAsignacionTerminal(
                request.getTerminalOrigenId(),
                request.getTerminalDestinoId(),
                request.getFecha(),
                request.getHoraSalida(),
                request.getHoraLlegada());

        if (!validacionTerminal.isPuedeAsignarse()) {
            return AsignacionResponse.builder()
                    .exitoso(false)
                    .mensaje("Error de terminal: " + validacionTerminal.getMensaje())
                    .build();
        }

        // 3. Registrar ocupación en terminales
        ocupacionTerminalService.registrarFrecuencia(
                request.getTerminalOrigenId(), request.getFecha(), request.getHoraSalida());
        ocupacionTerminalService.registrarFrecuencia(
                request.getTerminalDestinoId(), request.getFecha(), request.getHoraLlegada());

        log.info("Frecuencia asignada exitosamente. Chofer: {}, Fecha: {}, Hora: {}",
                request.getChoferId(), request.getFecha(), request.getHoraSalida());

        return AsignacionResponse.builder()
                .exitoso(true)
                .mensaje("Frecuencia asignada correctamente")
                .validacionChofer(validacionChofer)
                .validacionTerminal(validacionTerminal)
                .build();
    }

    /**
     * Obtiene el resumen de horas de un chofer para la semana
     */
    @Transactional(readOnly = true)
    public ResumenHorasChoferResponse obtenerResumenHorasSemana(Long choferId, LocalDate fecha) {
        UsuarioCooperativa chofer = usuarioCooperativaRepository.findById(choferId)
                .orElseThrow(() -> new RuntimeException("Chofer no encontrado: " + choferId));

        LocalDate inicioSemana = fecha.with(DayOfWeek.MONDAY);
        LocalDate finSemana = fecha.with(DayOfWeek.SUNDAY);

        int totalMinutosSemana = 0;
        int diasConJornadaExtendida = 0;
        List<ResumenDiaResponse> diasSemana = new java.util.ArrayList<>();

        for (LocalDate dia = inicioSemana; !dia.isAfter(finSemana); dia = dia.plusDays(1)) {
            int minutosDia = calcularMinutosTrabajadosEnDia(choferId, dia);
            int horasDia = minutosDia / 60;
            
            totalMinutosSemana += minutosDia;
            
            boolean jornadaExtendida = horasDia > HORAS_TRABAJO_NORMAL;
            if (jornadaExtendida) diasConJornadaExtendida++;

            diasSemana.add(ResumenDiaResponse.builder()
                    .fecha(dia)
                    .diaSemana(dia.getDayOfWeek().toString())
                    .horasTrabajadas(horasDia)
                    .minutosTrabajados(minutosDia)
                    .jornadaExtendida(jornadaExtendida)
                    .build());
        }

        return ResumenHorasChoferResponse.builder()
                .choferId(choferId)
                .choferNombre(chofer.getNombreCompleto())
                .semanaInicio(inicioSemana)
                .semanaFin(finSemana)
                .totalHorasSemana(totalMinutosSemana / 60)
                .totalMinutosSemana(totalMinutosSemana)
                .diasConJornadaExtendida(diasConJornadaExtendida)
                .diasRestantesJornadaExtendida(Math.max(0, DIAS_PERMITIDOS_10_HORAS - diasConJornadaExtendida))
                .diasSemana(diasSemana)
                .build();
    }

    // Métodos auxiliares privados

    private int calcularMinutosTrabajadosEnDia(Long choferId, LocalDate fecha) {
        List<Viaje> viajes = viajeRepository.findByChoferIdAndFecha(choferId, fecha);
        return viajes.stream()
                .map(v -> v.getFrecuencia())
                .filter(f -> f != null)
                .mapToInt(f -> f.getDuracionEstimadaMin() != null ? f.getDuracionEstimadaMin() : 0)
                .sum();
    }

    private int contarDiasJornadaExtendidaSemana(Long choferId, LocalDate fecha) {
        LocalDate inicioSemana = fecha.with(DayOfWeek.MONDAY);
        LocalDate finSemana = fecha.with(DayOfWeek.SUNDAY);
        
        int contador = 0;
        for (LocalDate dia = inicioSemana; !dia.isAfter(finSemana); dia = dia.plusDays(1)) {
            int minutos = calcularMinutosTrabajadosEnDia(choferId, dia);
            if (minutos > HORAS_TRABAJO_NORMAL * 60) {
                contador++;
            }
        }
        return contador;
    }

    private boolean tienJornadaExtendidaHoy(Long choferId, LocalDate fecha) {
        int minutos = calcularMinutosTrabajadosEnDia(choferId, fecha);
        return minutos > HORAS_TRABAJO_NORMAL * 60;
    }

    // DTOs internos
    @lombok.Data
    @lombok.Builder
    public static class ValidacionChoferResponse {
        private Long choferId;
        private String choferNombre;
        private LocalDate fecha;
        private int horasTrabajadasHoy;
        private int minutosTrabajadasHoy;
        private int horasPropuestas;
        private int limiteHorasHoy;
        private int diasJornadaExtendidaSemana;
        private boolean puedeAsignarse;
        private String mensaje;
    }

    @lombok.Data
    @lombok.Builder
    public static class ValidacionTerminalResponse {
        private Long terminalOrigenId;
        private String terminalOrigenNombre;
        private Long terminalDestinoId;
        private String terminalDestinoNombre;
        private LocalDate fecha;
        private LocalTime horaSalida;
        private LocalTime horaLlegada;
        private boolean origenDisponible;
        private boolean destinoDisponible;
        private boolean puedeAsignarse;
        private String mensaje;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AsignacionRequest {
        private Long frecuenciaViajeId;
        private Long choferId;
        private Long busId;
        private Long terminalOrigenId;
        private Long terminalDestinoId;
        private LocalDate fecha;
        private LocalTime horaSalida;
        private LocalTime horaLlegada;
        private int duracionMinutos;
    }

    @lombok.Data
    @lombok.Builder
    public static class AsignacionResponse {
        private boolean exitoso;
        private String mensaje;
        private ValidacionChoferResponse validacionChofer;
        private ValidacionTerminalResponse validacionTerminal;
    }

    @lombok.Data
    @lombok.Builder
    public static class ResumenHorasChoferResponse {
        private Long choferId;
        private String choferNombre;
        private LocalDate semanaInicio;
        private LocalDate semanaFin;
        private int totalHorasSemana;
        private int totalMinutosSemana;
        private int diasConJornadaExtendida;
        private int diasRestantesJornadaExtendida;
        private List<ResumenDiaResponse> diasSemana;
    }

    @lombok.Data
    @lombok.Builder
    public static class ResumenDiaResponse {
        private LocalDate fecha;
        private String diaSemana;
        private int horasTrabajadas;
        private int minutosTrabajados;
        private boolean jornadaExtendida;
    }
}
