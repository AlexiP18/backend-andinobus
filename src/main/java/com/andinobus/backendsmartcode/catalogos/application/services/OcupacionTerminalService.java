package com.andinobus.backendsmartcode.catalogos.application.services;

import com.andinobus.backendsmartcode.catalogos.domain.entities.OcupacionTerminal;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Terminal;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.OcupacionTerminalRepository;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.TerminalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestionar la ocupación y capacidad de los terminales.
 * Implementa la lógica de negocio para:
 * - Validar disponibilidad de frecuencias
 * - Prevenir congestión en terminales
 * - Calcular ocupación por hora/día
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OcupacionTerminalService {

    private final OcupacionTerminalRepository ocupacionRepository;
    private final TerminalRepository terminalRepository;

    /**
     * Calcula el máximo de frecuencias por hora para un terminal.
     * Fórmula: andenes (ya que cada andén puede tener 1 bus a la vez)
     */
    public int calcularMaxFrecuenciasPorHora(Terminal terminal) {
        // Máximo de buses que pueden estar en el terminal simultáneamente
        return terminal.getAndenes();
    }

    /**
     * Verifica si un terminal tiene disponibilidad para una frecuencia en una hora específica
     */
    @Transactional(readOnly = true)
    public boolean tieneDisponibilidad(Long terminalId, LocalDate fecha, LocalTime hora) {
        Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new RuntimeException("Terminal no encontrado: " + terminalId));

        int maxPorHora = calcularMaxFrecuenciasPorHora(terminal);

        OcupacionTerminal ocupacion = ocupacionRepository
                .findByTerminalAndFechaAndHora(terminal, fecha, hora)
                .orElse(null);

        if (ocupacion == null) {
            return true; // Sin ocupación registrada, hay disponibilidad
        }

        return ocupacion.puedeAgregarFrecuencia(maxPorHora);
    }

    /**
     * Verifica disponibilidad en un rango de horas (útil para evitar congestión en horas pico)
     */
    @Transactional(readOnly = true)
    public DisponibilidadRangoResponse verificarDisponibilidadRango(
            Long terminalId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        
        Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new RuntimeException("Terminal no encontrado: " + terminalId));

        List<OcupacionTerminal> ocupaciones = ocupacionRepository
                .findByTerminalIdAndFechaAndRangoHora(terminalId, fecha, horaInicio, horaFin);

        int maxPorHora = calcularMaxFrecuenciasPorHora(terminal);
        List<LocalTime> horasSaturadas = new ArrayList<>();
        List<LocalTime> horasDisponibles = new ArrayList<>();

        // Generar todas las horas en el rango (cada 15 minutos)
        LocalTime hora = horaInicio;
        while (!hora.isAfter(horaFin)) {
            LocalTime horaActual = hora;
            OcupacionTerminal ocupacion = ocupaciones.stream()
                    .filter(o -> o.getHora().equals(horaActual))
                    .findFirst()
                    .orElse(null);

            if (ocupacion == null || ocupacion.getFrecuenciasAsignadas() < maxPorHora) {
                horasDisponibles.add(horaActual);
            } else {
                horasSaturadas.add(horaActual);
            }

            hora = hora.plusMinutes(15);
        }

        return DisponibilidadRangoResponse.builder()
                .terminalId(terminalId)
                .terminalNombre(terminal.getNombre())
                .fecha(fecha)
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .maxFrecuenciasPorHora(maxPorHora)
                .horasDisponibles(horasDisponibles)
                .horasSaturadas(horasSaturadas)
                .porcentajeDisponibilidad(calcularPorcentaje(horasDisponibles.size(), 
                        horasDisponibles.size() + horasSaturadas.size()))
                .build();
    }

    /**
     * Registra una nueva frecuencia asignada a un terminal
     */
    @Transactional
    public void registrarFrecuencia(Long terminalId, LocalDate fecha, LocalTime hora) {
        Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new RuntimeException("Terminal no encontrado: " + terminalId));

        int maxPorHora = calcularMaxFrecuenciasPorHora(terminal);

        OcupacionTerminal ocupacion = ocupacionRepository
                .findByTerminalAndFechaAndHora(terminal, fecha, hora)
                .orElse(OcupacionTerminal.builder()
                        .terminal(terminal)
                        .fecha(fecha)
                        .hora(hora)
                        .frecuenciasAsignadas(0)
                        .build());

        if (!ocupacion.puedeAgregarFrecuencia(maxPorHora)) {
            throw new RuntimeException(String.format(
                    "El terminal %s no tiene capacidad disponible para la hora %s en la fecha %s. " +
                    "Capacidad: %d, Asignadas: %d",
                    terminal.getNombre(), hora, fecha, maxPorHora, ocupacion.getFrecuenciasAsignadas()));
        }

        ocupacion.incrementarFrecuencias();
        ocupacionRepository.save(ocupacion);

        log.info("Frecuencia registrada en terminal {} para {} a las {}. Total: {}/{}",
                terminal.getNombre(), fecha, hora, ocupacion.getFrecuenciasAsignadas(), maxPorHora);
    }

    /**
     * Elimina una frecuencia de un terminal (cuando se cancela o modifica)
     */
    @Transactional
    public void eliminarFrecuencia(Long terminalId, LocalDate fecha, LocalTime hora) {
        Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new RuntimeException("Terminal no encontrado: " + terminalId));

        ocupacionRepository.findByTerminalAndFechaAndHora(terminal, fecha, hora)
                .ifPresent(ocupacion -> {
                    ocupacion.decrementarFrecuencias();
                    if (ocupacion.getFrecuenciasAsignadas() <= 0) {
                        ocupacionRepository.delete(ocupacion);
                    } else {
                        ocupacionRepository.save(ocupacion);
                    }
                    log.info("Frecuencia eliminada del terminal {} para {} a las {}",
                            terminal.getNombre(), fecha, hora);
                });
    }

    /**
     * Obtiene el resumen de ocupación de un terminal para un día
     */
    @Transactional(readOnly = true)
    public OcupacionDiariaResponse obtenerOcupacionDiaria(Long terminalId, LocalDate fecha) {
        Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new RuntimeException("Terminal no encontrado: " + terminalId));

        List<OcupacionTerminal> ocupaciones = ocupacionRepository
                .findByTerminalIdAndFecha(terminalId, fecha);

        Long totalFrecuencias = ocupacionRepository.getTotalFrecuenciasDelDia(terminalId, fecha);
        int maxDiario = terminal.getMaxFrecuenciasDiarias();
        int maxPorHora = calcularMaxFrecuenciasPorHora(terminal);

        long horasSaturadas = ocupacionRepository.countHorasSaturadas(terminalId, fecha, maxPorHora);

        return OcupacionDiariaResponse.builder()
                .terminalId(terminalId)
                .terminalNombre(terminal.getNombre())
                .tipologia(terminal.getTipologia())
                .fecha(fecha)
                .totalFrecuenciasAsignadas(totalFrecuencias != null ? totalFrecuencias.intValue() : 0)
                .maxFrecuenciasDiarias(maxDiario)
                .maxFrecuenciasPorHora(maxPorHora)
                .horasSaturadas((int) horasSaturadas)
                .porcentajeOcupacion(calcularPorcentaje(
                        totalFrecuencias != null ? totalFrecuencias.intValue() : 0, maxDiario))
                .ocupacionesPorHora(ocupaciones.stream()
                        .map(o -> OcupacionHoraResponse.builder()
                                .hora(o.getHora())
                                .frecuenciasAsignadas(o.getFrecuenciasAsignadas())
                                .maxFrecuencias(maxPorHora)
                                .saturado(o.getFrecuenciasAsignadas() >= maxPorHora)
                                .build())
                        .toList())
                .build();
    }

    /**
     * Sugiere las mejores horas para asignar una frecuencia (menos congestionadas)
     */
    @Transactional(readOnly = true)
    public List<SugerenciaHoraResponse> sugerirHorasOptimas(Long terminalId, LocalDate fecha, int cantidad) {
        Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new RuntimeException("Terminal no encontrado: " + terminalId));

        int maxPorHora = calcularMaxFrecuenciasPorHora(terminal);
        List<OcupacionTerminal> ocupaciones = ocupacionRepository
                .findByTerminalIdAndFecha(terminalId, fecha);

        List<SugerenciaHoraResponse> sugerencias = new ArrayList<>();

        // Generar todas las horas del día (5:00 - 23:00) cada 15 minutos
        LocalTime hora = LocalTime.of(5, 0);
        LocalTime horaFin = LocalTime.of(23, 0);

        while (!hora.isAfter(horaFin)) {
            LocalTime horaActual = hora;
            int ocupacionActual = ocupaciones.stream()
                    .filter(o -> o.getHora().equals(horaActual))
                    .findFirst()
                    .map(OcupacionTerminal::getFrecuenciasAsignadas)
                    .orElse(0);

            if (ocupacionActual < maxPorHora) {
                sugerencias.add(SugerenciaHoraResponse.builder()
                        .hora(horaActual)
                        .ocupacionActual(ocupacionActual)
                        .disponibilidad(maxPorHora - ocupacionActual)
                        .prioridad(calcularPrioridad(horaActual, ocupacionActual, maxPorHora))
                        .build());
            }

            hora = hora.plusMinutes(15);
        }

        // Ordenar por prioridad (menos ocupadas primero, preferir horas normales)
        return sugerencias.stream()
                .sorted((a, b) -> Integer.compare(b.getPrioridad(), a.getPrioridad()))
                .limit(cantidad)
                .toList();
    }

    private double calcularPorcentaje(int valor, int total) {
        if (total == 0) return 0;
        return Math.round(((double) valor / total * 100) * 100.0) / 100.0;
    }

    private int calcularPrioridad(LocalTime hora, int ocupacion, int max) {
        int prioridad = 100 - (int) ((double) ocupacion / max * 100);

        // Bonus para horas no pico (evitar 6-8 AM y 5-7 PM)
        int horaNum = hora.getHour();
        if (horaNum >= 9 && horaNum <= 11 || horaNum >= 14 && horaNum <= 16) {
            prioridad += 10; // Horas preferidas
        }
        if (horaNum >= 6 && horaNum <= 8 || horaNum >= 17 && horaNum <= 19) {
            prioridad -= 10; // Horas pico
        }

        return prioridad;
    }

    // DTOs internos
    @lombok.Data
    @lombok.Builder
    public static class DisponibilidadRangoResponse {
        private Long terminalId;
        private String terminalNombre;
        private LocalDate fecha;
        private LocalTime horaInicio;
        private LocalTime horaFin;
        private int maxFrecuenciasPorHora;
        private List<LocalTime> horasDisponibles;
        private List<LocalTime> horasSaturadas;
        private double porcentajeDisponibilidad;
    }

    @lombok.Data
    @lombok.Builder
    public static class OcupacionDiariaResponse {
        private Long terminalId;
        private String terminalNombre;
        private String tipologia;
        private LocalDate fecha;
        private int totalFrecuenciasAsignadas;
        private int maxFrecuenciasDiarias;
        private int maxFrecuenciasPorHora;
        private int horasSaturadas;
        private double porcentajeOcupacion;
        private List<OcupacionHoraResponse> ocupacionesPorHora;
    }

    @lombok.Data
    @lombok.Builder
    public static class OcupacionHoraResponse {
        private LocalTime hora;
        private int frecuenciasAsignadas;
        private int maxFrecuencias;
        private boolean saturado;
    }

    @lombok.Data
    @lombok.Builder
    public static class SugerenciaHoraResponse {
        private LocalTime hora;
        private int ocupacionActual;
        private int disponibilidad;
        private int prioridad;
    }
}
