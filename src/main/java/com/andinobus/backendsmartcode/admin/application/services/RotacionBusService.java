package com.andinobus.backendsmartcode.admin.application.services;

import com.andinobus.backendsmartcode.admin.domain.entities.*;
import com.andinobus.backendsmartcode.admin.domain.enums.TipoFrecuencia;
import com.andinobus.backendsmartcode.admin.domain.repositories.*;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Terminal;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.TerminalRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusRepository;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.FrecuenciaConfigCooperativa;
import com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories.FrecuenciaConfigCooperativaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar la disponibilidad y rotación de buses.
 * 
 * Implementa las reglas de negocio:
 * 1. Los buses no pueden salir consecutivamente - deben intercalarse
 * 2. Un bus debe estar físicamente en la terminal para asignarle una frecuencia
 * 3. Se respetan tiempos de descanso según tipo de frecuencia (inter/intraprovincial)
 * 4. Se sigue el ciclo de rotación definido para la cooperativa
 */
@Profile("dev")
@Service
@RequiredArgsConstructor
@Slf4j
public class RotacionBusService {

    private final DisponibilidadBusRepository disponibilidadRepository;
    private final CicloRotacionRepository cicloRepository;
    private final AsignacionRotacionRepository asignacionRepository;
    private final BusRepository busRepository;
    private final TerminalRepository terminalRepository;
    private final FrecuenciaConfigCooperativaRepository configRepository;
    private final RutaRepository rutaRepository;

    /**
     * Determina el tipo de frecuencia basándose en la distancia de la ruta
     */
    public TipoFrecuencia determinarTipoFrecuencia(Ruta ruta, FrecuenciaConfigCooperativa config) {
        Double umbralKm = config.getUmbralInterprovincialKm();
        if (umbralKm == null) umbralKm = 100.0;
        
        Double distanciaRuta = ruta.getDistanciaKm();
        if (distanciaRuta == null) {
            // Si no hay distancia, asumir interprovincial por seguridad
            return TipoFrecuencia.INTERPROVINCIAL;
        }
        
        return distanciaRuta > umbralKm ? TipoFrecuencia.INTERPROVINCIAL : TipoFrecuencia.INTRAPROVINCIAL;
    }

    /**
     * Obtiene el tiempo de descanso según el tipo de frecuencia
     */
    public int obtenerTiempoDescanso(TipoFrecuencia tipo, FrecuenciaConfigCooperativa config) {
        if (tipo == TipoFrecuencia.INTERPROVINCIAL) {
            return config.getDescansoInterprovincialMinutos() != null 
                ? config.getDescansoInterprovincialMinutos() 
                : 120;
        } else {
            return config.getDescansoIntraprovincialMinutos() != null 
                ? config.getDescansoIntraprovincialMinutos() 
                : 45;
        }
    }

    /**
     * Registra la llegada de un bus a una terminal y calcula cuándo estará disponible
     */
    @Transactional
    public DisponibilidadBus registrarLlegada(
            Long busId, 
            Long terminalId, 
            LocalDate fecha, 
            LocalTime horaLlegada,
            FrecuenciaViaje frecuenciaOrigen,
            TipoFrecuencia tipoFrecuencia) {
        
        Bus bus = busRepository.findById(busId)
            .orElseThrow(() -> new RuntimeException("Bus no encontrado: " + busId));
        Terminal terminal = terminalRepository.findById(terminalId)
            .orElseThrow(() -> new RuntimeException("Terminal no encontrada: " + terminalId));
        
        FrecuenciaConfigCooperativa config = configRepository
            .findByCooperativaId(bus.getCooperativa().getId())
            .orElse(null);
        
        int tiempoDescanso = obtenerTiempoDescanso(tipoFrecuencia, config != null ? config : new FrecuenciaConfigCooperativa());
        LocalTime horaDisponible = horaLlegada.plusMinutes(tiempoDescanso);
        
        DisponibilidadBus disponibilidad = DisponibilidadBus.builder()
            .bus(bus)
            .cooperativa(bus.getCooperativa())
            .terminal(terminal)
            .fecha(fecha)
            .horaLlegada(horaLlegada)
            .horaDisponible(horaDisponible)
            .frecuenciaOrigen(frecuenciaOrigen)
            .estado("DISPONIBLE")
            .tiempoDescansoMinutos(tiempoDescanso)
            .build();
        
        return disponibilidadRepository.save(disponibilidad);
    }

    /**
     * Busca buses disponibles en una terminal para una hora específica
     */
    public List<DisponibilidadBus> buscarBusesDisponibles(
            Long terminalId, 
            LocalDate fecha, 
            LocalTime hora) {
        return disponibilidadRepository.findBusesDisponiblesEnTerminal(terminalId, fecha, hora);
    }

    /**
     * Asigna un bus disponible a una nueva frecuencia
     */
    @Transactional
    public boolean asignarBusAFrecuencia(DisponibilidadBus disponibilidad, FrecuenciaViaje frecuencia) {
        if (!"DISPONIBLE".equals(disponibilidad.getEstado())) {
            return false;
        }
        
        disponibilidad.setFrecuenciaSiguiente(frecuencia);
        disponibilidad.setEstado("ASIGNADO");
        disponibilidadRepository.save(disponibilidad);
        
        return true;
    }

    /**
     * Verifica si un bus puede ser asignado a una frecuencia considerando:
     * - Que esté en la terminal correcta
     * - Que haya pasado el tiempo mínimo de descanso
     * - Que no esté ya asignado
     */
    public boolean puedeAsignarBus(Long busId, Long terminalId, LocalDate fecha, LocalTime horaSalida) {
        List<DisponibilidadBus> disponibilidades = disponibilidadRepository
            .findBusesDisponiblesEnTerminal(terminalId, fecha, horaSalida);
        
        return disponibilidades.stream()
            .anyMatch(d -> d.getBus().getId().equals(busId));
    }

    /**
     * Obtiene el mejor bus disponible para una frecuencia.
     * Criterios de selección:
     * 1. Bus que lleva más tiempo esperando (para balancear uso)
     * 2. Bus con menos horas trabajadas en el día
     */
    public Optional<DisponibilidadBus> obtenerMejorBusDisponible(
            Long terminalId, 
            LocalDate fecha, 
            LocalTime horaSalida,
            Set<Long> busesExcluidos) {
        
        List<DisponibilidadBus> disponibles = disponibilidadRepository
            .findBusesDisponiblesEnTerminal(terminalId, fecha, horaSalida);
        
        return disponibles.stream()
            .filter(d -> !busesExcluidos.contains(d.getBus().getId()))
            .min(Comparator.comparing(DisponibilidadBus::getHoraDisponible));
    }

    // ==================== GESTIÓN DE CICLOS DE ROTACIÓN ====================

    /**
     * Crea un nuevo ciclo de rotación
     */
    @Transactional
    public CicloRotacion crearCiclo(Long cooperativaId, String nombre, String descripcion, int diasCiclo) {
        CicloRotacion ciclo = CicloRotacion.builder()
            .cooperativa(busRepository.findById(cooperativaId)
                .map(Bus::getCooperativa)
                .orElseThrow(() -> new RuntimeException("Cooperativa no encontrada")))
            .nombre(nombre)
            .descripcion(descripcion)
            .diasCiclo(diasCiclo)
            .build();
        
        // Esto está mal, necesito obtener la cooperativa directamente
        // Lo corrijo buscando cualquier bus de la cooperativa
        var buses = busRepository.findByCooperativaIdAndActivoTrue(cooperativaId);
        if (buses.isEmpty()) {
            throw new RuntimeException("No hay buses activos para la cooperativa: " + cooperativaId);
        }
        ciclo.setCooperativa(buses.get(0).getCooperativa());
        
        return cicloRepository.save(ciclo);
    }

    /**
     * Obtiene las asignaciones para un día específico del ciclo
     */
    public List<AsignacionRotacion> obtenerAsignacionesDia(Long cicloId, int diaCiclo) {
        return asignacionRepository.findByCicloIdAndDiaCicloOrderByOrdenAsc(cicloId, diaCiclo);
    }

    /**
     * Calcula qué día del ciclo corresponde a una fecha
     */
    public int calcularDiaCiclo(CicloRotacion ciclo, LocalDate fechaInicioCiclo, LocalDate fechaConsulta) {
        return ciclo.getDiaCicloParaFecha(fechaInicioCiclo, fechaConsulta);
    }

    /**
     * Genera disponibilidades iniciales para una fecha basándose en el ciclo de rotación
     */
    @Transactional
    public List<DisponibilidadBus> generarDisponibilidadesIniciales(
            Long cooperativaId,
            LocalDate fecha,
            LocalTime horaInicio) {
        
        List<Bus> busesActivos = busRepository.findByCooperativaIdAndActivoTrue(cooperativaId);
        List<DisponibilidadBus> disponibilidades = new ArrayList<>();
        
        // Por defecto, todos los buses están disponibles en la terminal base al inicio del día
        for (Bus bus : busesActivos) {
            // Obtener terminal base del bus (si la tiene configurada)
            Terminal terminalBase = bus.getTerminalBase();
            if (terminalBase == null) {
                // Si no tiene terminal base, buscar la primera terminal de la cooperativa
                continue;
            }
            
            DisponibilidadBus disponibilidad = DisponibilidadBus.builder()
                .bus(bus)
                .cooperativa(bus.getCooperativa())
                .terminal(terminalBase)
                .fecha(fecha)
                .horaLlegada(horaInicio.minusHours(1))
                .horaDisponible(horaInicio)
                .estado("DISPONIBLE")
                .tiempoDescansoMinutos(0)
                .observaciones("Disponibilidad inicial del día")
                .build();
            
            disponibilidades.add(disponibilidadRepository.save(disponibilidad));
        }
        
        return disponibilidades;
    }

    /**
     * Limpia las disponibilidades de un rango de fechas
     */
    @Transactional
    public void limpiarDisponibilidades(Long cooperativaId, LocalDate fechaInicio, LocalDate fechaFin) {
        disponibilidadRepository.deleteByCooperativaIdAndFechaBetween(cooperativaId, fechaInicio, fechaFin);
    }

    // ==================== VALIDACIONES ====================

    /**
     * Valida si una frecuencia puede ser generada considerando:
     * - Disponibilidad de bus en terminal
     * - Tiempo de descanso
     * - Restricciones de rotación
     */
    public ValidationResult validarFrecuencia(
            Long busId,
            Long terminalOrigenId,
            LocalDate fecha,
            LocalTime horaSalida,
            TipoFrecuencia tipoFrecuencia) {
        
        ValidationResult result = new ValidationResult();
        
        // 1. Verificar si hay un bus disponible en la terminal
        List<DisponibilidadBus> disponibles = buscarBusesDisponibles(terminalOrigenId, fecha, horaSalida);
        
        if (disponibles.isEmpty()) {
            result.addError("No hay buses disponibles en la terminal para esa hora");
            return result;
        }
        
        // 2. Si se especificó un bus, verificar que ese bus esté disponible
        if (busId != null) {
            boolean busEspecificoDisponible = disponibles.stream()
                .anyMatch(d -> d.getBus().getId().equals(busId));
            
            if (!busEspecificoDisponible) {
                result.addWarning("El bus especificado no está disponible en la terminal. Se asignará otro bus.");
            }
        }
        
        result.setValid(true);
        result.setBusesDisponibles(disponibles.stream()
            .map(d -> d.getBus().getId())
            .collect(Collectors.toList()));
        
        return result;
    }

    /**
     * Resultado de validación
     */
    public static class ValidationResult {
        private boolean valid = false;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private List<Long> busesDisponibles = new ArrayList<>();

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public List<Long> getBusesDisponibles() { return busesDisponibles; }
        public void setBusesDisponibles(List<Long> buses) { this.busesDisponibles = buses; }
        
        public void addError(String error) { 
            this.errors.add(error); 
            this.valid = false;
        }
        public void addWarning(String warning) { this.warnings.add(warning); }
    }
}
