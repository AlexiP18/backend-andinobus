package com.andinobus.backendsmartcode.cooperativa.application.services;

import com.andinobus.backendsmartcode.admin.domain.entities.FrecuenciaViaje;
import com.andinobus.backendsmartcode.admin.domain.entities.Ruta;
import com.andinobus.backendsmartcode.admin.domain.enums.TipoFrecuencia;
import com.andinobus.backendsmartcode.admin.domain.repositories.DisponibilidadBusRepository;
import com.andinobus.backendsmartcode.admin.domain.repositories.FrecuenciaViajeRepository;
import com.andinobus.backendsmartcode.admin.domain.repositories.RutaRepository;
import com.andinobus.backendsmartcode.catalogos.domain.entities.*;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.CooperativaTerminalRepository;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.TerminalRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.*;
import com.andinobus.backendsmartcode.catalogos.domain.Camino;
import com.andinobus.backendsmartcode.cooperativa.api.dto.GeneracionInteligenteDtos.*;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.FrecuenciaConfigCooperativa;
import com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories.FrecuenciaConfigCooperativaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de generación inteligente de frecuencias.
 * 
 * Reglas de negocio:
 * 1. Cada bus opera en circuito (A→B→A) recogiendo pasajeros en ambos sentidos
 * 2. Los buses deben estar físicamente en el terminal para poder salir
 * 3. Se respetan tiempos de descanso entre viajes
 * 4. Choferes trabajan máximo 8h normales, 10h excepcionales (máx 2 días/semana)
 * 5. Paradas intermedias solo en viajes interprovinciales:
 *    - 1-3h: máx 1 parada
 *    - 4-6h: máx 2 paradas  
 *    - >8h: máx 3 paradas
 * 6. Viajes intraprovinciales: sin paradas intermedias
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeneracionFrecuenciasInteligenteService {

    private final CooperativaTerminalRepository cooperativaTerminalRepository;
    private final TerminalRepository terminalRepository;
    private final BusRepository busRepository;
    private final BusChoferRepository busChoferRepository;
    private final FrecuenciaViajeRepository frecuenciaViajeRepository;
    private final FrecuenciaConfigCooperativaRepository configRepository;
    private final CooperativaRepository cooperativaRepository;
    private final RutaRepository rutaRepository;
    private final DisponibilidadBusRepository disponibilidadBusRepository;
    private final CaminoRepository caminoRepository;
    private final com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories.UsuarioCooperativaRepository usuarioCooperativaRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Estado de cada bus durante la simulación
     */
    private static class EstadoBus {
        Long busId;
        String placa;
        Integer asientosDisponibles;
        Long terminalActualId;
        String terminalActualNombre;
        LocalTime horaDisponible;
        double horasTrabajadasChofer;
        Long choferId;
        String choferNombre;
        int viajesHoy;
        int ordenDiaActual;  // Contador de orden para el día
        
        EstadoBus(Bus bus, Long terminalInicial, String terminalNombre) {
            this.busId = bus.getId();
            this.placa = bus.getPlaca();
            this.asientosDisponibles = bus.getCapacidadAsientos() != null ? bus.getCapacidadAsientos() : 40;
            this.terminalActualId = terminalInicial;
            this.terminalActualNombre = terminalNombre;
            this.horaDisponible = LocalTime.of(5, 0); // Comienza a las 5:00 AM
            this.horasTrabajadasChofer = 0;
            this.viajesHoy = 0;
            this.ordenDiaActual = 0;
        }
        
        void resetDia(LocalTime horaInicio) {
            this.horaDisponible = horaInicio;
            this.horasTrabajadasChofer = 0;
            this.viajesHoy = 0;
            this.ordenDiaActual = 0;
        }
    }
    
    /**
     * Clase para rastrear las horas trabajadas por chofer en la semana.
     * Un chofer puede trabajar máximo 10 horas/día pero solo 2 días así por semana.
     * Los demás días está limitado a 8 horas.
     */
    private static class EstadoChoferSemanal {
        Long choferId;
        String nombreChofer;
        int diasExtendidos; // Días que ha trabajado más de 8 horas (máx 2 por semana)
        double horasTotalesSemana;
        int semanaActual; // Para resetear al cambiar de semana
        
        EstadoChoferSemanal(Long choferId, String nombre) {
            this.choferId = choferId;
            this.nombreChofer = nombre;
            this.diasExtendidos = 0;
            this.horasTotalesSemana = 0;
            this.semanaActual = -1;
        }
        
        /**
         * Retorna las horas máximas que puede trabajar el chofer hoy.
         * - Si ya usó sus 2 días extendidos: máximo 8 horas
         * - Si aún le quedan días extendidos: máximo 10 horas
         */
        int getMaxHorasHoy() {
            return diasExtendidos >= 2 ? 8 : 10;
        }
        
        /**
         * Registra las horas trabajadas en un día.
         * Si trabajó más de 8 horas, cuenta como día extendido.
         */
        void registrarDia(double horasTrabajadas) {
            horasTotalesSemana += horasTrabajadas;
            if (horasTrabajadas > 8) {
                diasExtendidos++;
            }
        }
        
        /**
         * Resetea el contador semanal al inicio de una nueva semana
         */
        void resetSemana(int nuevaSemana) {
            if (this.semanaActual != nuevaSemana) {
                this.semanaActual = nuevaSemana;
                this.diasExtendidos = 0;
                this.horasTotalesSemana = 0;
            }
        }
    }

    /**
     * Obtiene el estado inicial para la generación inteligente
     */
    @Transactional(readOnly = true)
    public EstadoGeneracionInteligente getEstado(Long cooperativaId) {
        // Buses disponibles
        List<Bus> buses = busRepository.findByCooperativaIdAndActivoTrue(cooperativaId)
                .stream()
                .filter(b -> "DISPONIBLE".equals(b.getEstado()))
                .collect(Collectors.toList());

        // Terminales asignados
        List<CooperativaTerminal> terminales = cooperativaTerminalRepository
                .findByCooperativaIdWithTerminal(cooperativaId);

        // Choferes
        List<BusChofer> asignaciones = busChoferRepository.findByCooperativaId(cooperativaId);
        int totalChoferes = (int) asignaciones.stream()
                .map(bc -> bc.getChofer().getId())
                .distinct()
                .count();

        // Generar rutas posibles aplicando reglas de negocio
        List<RutaCircuito> rutas = generarRutasValidas(terminales);

        // Configuración con valores por defecto seguros
        // NOTA: maxHorasChofer=10 (un chofer puede trabajar hasta 10h/día pero solo 2 días así por semana)
        var config = configRepository.findByCooperativaId(cooperativaId).orElse(null);
        
        ConfiguracionGeneracion configGen = ConfiguracionGeneracion.builder()
                .maxHorasChofer(getConfigValue(config, FrecuenciaConfigCooperativa::getMaxHorasDiariasChofer, 10))
                .maxHorasExcepcionales(getConfigValue(config, FrecuenciaConfigCooperativa::getMaxHorasExcepcionales, 12))
                // Descansos base reducidos - el sistema usa cálculo dinámico (25% del viaje)
                .descansoInterprovincialMin(getConfigValue(config, FrecuenciaConfigCooperativa::getDescansoInterprovincialMinutos, 60))
                .descansoIntraprovincialMin(getConfigValue(config, FrecuenciaConfigCooperativa::getDescansoIntraprovincialMinutos, 30))
                .umbralInterprovincialKm(config != null && config.getUmbralInterprovincialKm() != null 
                        ? config.getUmbralInterprovincialKm().intValue() : 100)
                .horaInicioOperacion(config != null && config.getHoraInicioOperacion() != null 
                        ? config.getHoraInicioOperacion().toString() : "05:00")
                .horaFinOperacion(config != null && config.getHoraFinOperacion() != null 
                        ? config.getHoraFinOperacion().toString() : "22:00")
                .build();

        // Calcular capacidad estimada
        int horasOperacionDia = 17; // 5AM a 10PM
        int viajesPromedioPorBus = (horasOperacionDia * 60) / 180; // ~5-6 viajes ida+vuelta por bus
        int capacidadDiaria = buses.size() * viajesPromedioPorBus;

        return EstadoGeneracionInteligente.builder()
                .busesDisponibles(buses.size())
                .choferesDisponibles(totalChoferes)
                .terminalesHabilitados(terminales.size())
                .rutasCircuito(rutas)
                .configuracion(configGen)
                .capacidadEstimadaDiaria(capacidadDiaria)
                .build();
    }

    /**
     * Genera las rutas válidas según las reglas de negocio:
     * - INTRAPROVINCIAL: entre cantones DIFERENTES de la MISMA provincia
     * - INTERPROVINCIAL: entre cantones de DIFERENTES provincias
     * - NO PERMITIDO: entre terminales del MISMO cantón (como origen-destino principal)
     */
    private List<RutaCircuito> generarRutasValidas(List<CooperativaTerminal> terminales) {
        List<RutaCircuito> rutas = new ArrayList<>();
        
        for (CooperativaTerminal origen : terminales) {
            for (CooperativaTerminal destino : terminales) {
                Terminal tOrigen = origen.getTerminal();
                Terminal tDestino = destino.getTerminal();
                
                // No permitir mismo terminal
                if (tOrigen.getId().equals(tDestino.getId())) {
                    continue;
                }
                
                // Obtener provincia y cantón (normalizados)
                String provOrigen = normalizarTexto(tOrigen.getProvincia());
                String provDestino = normalizarTexto(tDestino.getProvincia());
                String cantonOrigen = normalizarTexto(tOrigen.getCanton());
                String cantonDestino = normalizarTexto(tDestino.getCanton());
                
                // Determinar tipo de ruta
                TipoFrecuencia tipo;
                boolean rutaValida = false;
                
                if (!provOrigen.equals(provDestino)) {
                    // Diferentes provincias = INTERPROVINCIAL
                    tipo = TipoFrecuencia.INTERPROVINCIAL;
                    rutaValida = true;
                } else if (!cantonOrigen.equals(cantonDestino)) {
                    // Misma provincia, diferentes cantones = INTRAPROVINCIAL
                    tipo = TipoFrecuencia.INTRAPROVINCIAL;
                    rutaValida = true;
                } else {
                    // Mismo cantón = NO PERMITIDO como ruta principal
                    // (Solo se permite como terminal alternativo de vuelta)
                    continue;
                }
                
                if (rutaValida) {
                    double distancia = calcularDistanciaKm(tOrigen, tDestino);
                    int duracion = calcularDuracionMinutos(distancia);
                    int maxParadas = calcularMaxParadasPermitidas(duracion, tipo);
                    
                    rutas.add(RutaCircuito.builder()
                            .terminalOrigenId(tOrigen.getId())
                            .terminalOrigenNombre(tOrigen.getNombre() + " (" + tOrigen.getCanton() + ")")
                            .terminalDestinoId(tDestino.getId())
                            .terminalDestinoNombre(tDestino.getNombre() + " (" + tDestino.getCanton() + ")")
                            .distanciaKm(Math.round(distancia * 10.0) / 10.0)
                            .duracionMinutos(duracion)
                            .tipoFrecuencia(tipo.name())
                            .maxParadasPermitidas(maxParadas)
                            .precioSugerido(calcularPrecioSugerido(distancia))
                            .provinciaOrigen(tOrigen.getProvincia())
                            .provinciaDestino(tDestino.getProvincia())
                            .cantonOrigen(tOrigen.getCanton())
                            .cantonDestino(tDestino.getCanton())
                            .build());
                }
            }
        }
        
        return rutas;
    }

    /**
     * Normaliza texto para comparación (quita acentos, mayúsculas, espacios extra)
     */
    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        return java.text.Normalizer.normalize(texto.toUpperCase().trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }

    /**
     * Obtiene valor de configuración con valor por defecto seguro
     */
    private <T> T getConfigValue(FrecuenciaConfigCooperativa config, 
                                  java.util.function.Function<FrecuenciaConfigCooperativa, T> getter, 
                                  T defaultValue) {
        if (config == null) return defaultValue;
        T value = getter.apply(config);
        return value != null ? value : defaultValue;
    }

    /**
     * Genera preview de frecuencias con lógica inteligente
     */
    @Transactional(readOnly = true)
    public PreviewGeneracionInteligente previewGeneracion(Long cooperativaId, GenerarInteligenteRequest request) {
        List<FrecuenciaPreview> frecuencias = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();
        List<String> errores = new ArrayList<>();
        Map<String, Integer> frecuenciasPorRuta = new HashMap<>();
        Map<String, Integer> frecuenciasPorBus = new HashMap<>();

        try {
            // Obtener configuración con valores por defecto seguros
            var config = configRepository.findByCooperativaId(cooperativaId).orElse(null);
            // NOTA: Un chofer puede trabajar máximo 10 horas/día pero solo 2 días así por semana
            int maxHorasChofer = getConfigValue(config, FrecuenciaConfigCooperativa::getMaxHorasDiariasChofer, 10);
            // Los tiempos de descanso ahora son dinámicos basados en duración del viaje
            // Estos valores se usan como respaldo si el cálculo dinámico falla
            int descansoInterprov = getConfigValue(config, FrecuenciaConfigCooperativa::getDescansoInterprovincialMinutos, 60);
            int descansoIntraprov = getConfigValue(config, FrecuenciaConfigCooperativa::getDescansoIntraprovincialMinutos, 30);
            int umbralKm = config != null && config.getUmbralInterprovincialKm() != null 
                    ? config.getUmbralInterprovincialKm().intValue() : 100;
            LocalTime horaInicioOp = config != null && config.getHoraInicioOperacion() != null 
                    ? config.getHoraInicioOperacion() : LocalTime.of(5, 0);
            LocalTime horaFinOp = config != null && config.getHoraFinOperacion() != null 
                    ? config.getHoraFinOperacion() : LocalTime.of(22, 0);

            // Obtener buses - aceptar DISPONIBLE o EN_SERVICIO (ambos pueden operar)
            List<Bus> busesTotales = busRepository.findByCooperativaIdAndActivoTrue(cooperativaId);
            log.info("Buses activos encontrados para cooperativa {}: {}", cooperativaId, busesTotales.size());
            
            List<Bus> buses = busesTotales.stream()
                    .filter(b -> {
                        String estado = b.getEstado();
                        boolean valido = estado == null || "DISPONIBLE".equals(estado) || "EN_SERVICIO".equals(estado);
                        if (!valido) {
                            log.debug("Bus {} excluido por estado: {}", b.getPlaca(), estado);
                        }
                        return valido;
                    })
                    .collect(Collectors.toList());
            
            log.info("Buses disponibles para generación: {}", buses.size());

            if (buses.isEmpty()) {
                errores.add("No hay buses disponibles. Total buses activos: " + busesTotales.size() + 
                        ". Verifique que los buses tengan estado DISPONIBLE o EN_SERVICIO.");
                return buildEmptyPreview(errores);
            }

            // Obtener terminales
            List<CooperativaTerminal> terminales = cooperativaTerminalRepository
                    .findByCooperativaIdWithTerminal(cooperativaId);

            if (terminales.isEmpty()) {
                errores.add("No hay terminales asignados a la cooperativa");
                return buildEmptyPreview(errores);
            }

            // Validar rutas seleccionadas
            List<RutaCircuitoRequest> rutasAOperar = request.getRutasCircuito();
            if (rutasAOperar == null || rutasAOperar.isEmpty()) {
                errores.add("Debe seleccionar al menos una ruta");
                return buildEmptyPreview(errores);
            }
            
            // LOG: Mostrar rutas recibidas
            log.info("========== RUTAS RECIBIDAS DEL FRONTEND ==========");
            log.info("Total rutas: {}", rutasAOperar.size());
            for (int i = 0; i < rutasAOperar.size(); i++) {
                RutaCircuitoRequest r = rutasAOperar.get(i);
                log.info("  Ruta {}: {} ({}) -> {} ({}), distancia={}km, duracion={}min", 
                        i, r.getTerminalOrigenNombre(), r.getTerminalOrigenId(),
                        r.getTerminalDestinoNombre(), r.getTerminalDestinoId(),
                        r.getDistanciaKm(), r.getDuracionMinutos());
            }
            log.info("==================================================");

            // Obtener choferes por bus
            Map<Long, List<BusChofer>> choferesPorBus = busChoferRepository.findByCooperativaId(cooperativaId)
                    .stream()
                    .collect(Collectors.groupingBy(bc -> bc.getBus().getId()));

            // NUEVO: Crear pool global de buses disponibles (sin asignar a terminales específicos)
            List<EstadoBus> poolBuses = new ArrayList<>();
            for (Bus bus : buses) {
                EstadoBus estadoBus = new EstadoBus(bus, null, null);
                
                // Asignar el primer chofer disponible del bus
                List<BusChofer> choferes = choferesPorBus.get(bus.getId());
                if (choferes != null && !choferes.isEmpty()) {
                    BusChofer asignacion = choferes.get(0);
                    estadoBus.choferId = asignacion.getChofer().getId();
                    estadoBus.choferNombre = asignacion.getChofer().getNombres() + " " + 
                            (asignacion.getChofer().getApellidos() != null ? asignacion.getChofer().getApellidos() : "");
                }
                poolBuses.add(estadoBus);
            }
            
            log.info("Pool global de {} buses creado para {} rutas", poolBuses.size(), rutasAOperar.size());

            // Validar fechas
            if (request.getFechaInicio() == null || request.getFechaFin() == null) {
                errores.add("Las fechas de inicio y fin son requeridas");
                return buildEmptyPreview(errores);
            }

        // Crear mapa de estado semanal por chofer
        // Un chofer puede trabajar máximo 10h/día pero solo 2 días así por semana
        Map<Long, EstadoChoferSemanal> estadoChoferesSemanal = new HashMap<>();
        for (EstadoBus bus : poolBuses) {
            if (bus.choferId != null && !estadoChoferesSemanal.containsKey(bus.choferId)) {
                estadoChoferesSemanal.put(bus.choferId, 
                    new EstadoChoferSemanal(bus.choferId, bus.choferNombre));
            }
        }
        log.info("Rastreando {} choferes para límite semanal de 10h (máx 2 días/semana)", 
                estadoChoferesSemanal.size());

        // Generar frecuencias por cada día
        LocalDate fecha = request.getFechaInicio();
        int totalDias = 0;
        int semanaAnterior = -1;

        while (!fecha.isAfter(request.getFechaFin())) {
            String diaSemana = obtenerDiaSemana(fecha);
            
            // Detectar cambio de semana (usando número de semana del año)
            int semanaActual = fecha.get(java.time.temporal.WeekFields.ISO.weekOfYear());
            if (semanaActual != semanaAnterior) {
                // Nueva semana: resetear contadores de días extendidos
                log.info("=== NUEVA SEMANA {} (fecha: {}) - Reseteando contadores de choferes ===", 
                        semanaActual, fecha);
                for (EstadoChoferSemanal estado : estadoChoferesSemanal.values()) {
                    estado.resetSemana(semanaActual);
                }
                semanaAnterior = semanaActual;
            }
            
            // Verificar si opera este día
            if (request.getDiasOperacion() != null && !request.getDiasOperacion().isEmpty() 
                    && !request.getDiasOperacion().contains(diaSemana)) {
                fecha = fecha.plusDays(1);
                continue;
            }

            totalDias++;
            
            // Calcular horas máximas por chofer para HOY basado en su estado semanal
            Map<Long, Integer> maxHorasPorChoferHoy = new HashMap<>();
            for (EstadoBus bus : poolBuses) {
                if (bus.choferId != null) {
                    EstadoChoferSemanal estadoChofer = estadoChoferesSemanal.get(bus.choferId);
                    int maxHoras = estadoChofer != null ? estadoChofer.getMaxHorasHoy() : 10;
                    maxHorasPorChoferHoy.put(bus.choferId, maxHoras);
                }
            }
            
            // Reiniciar estado de buses para el nuevo día
            for (EstadoBus estado : poolBuses) {
                estado.resetDia(horaInicioOp);
            }

            // NUEVO: Generar viajes del día usando pool global de buses
            LocalDate fechaActual = fecha;
            generarViajesDelDiaV2(
                    fechaActual, diaSemana, rutasAOperar, poolBuses,
                    horaInicioOp, horaFinOp, maxHorasChofer,
                    descansoInterprov, descansoIntraprov, umbralKm,
                    frecuencias, frecuenciasPorRuta, frecuenciasPorBus,
                    advertencias, request.getPermitirParadas(), request.getMaxParadasPersonalizado(),
                    maxHorasPorChoferHoy // Nuevo parámetro: horas máximas personalizadas por chofer
            );
            
            // Al final del día, registrar horas trabajadas por cada chofer
            for (EstadoBus bus : poolBuses) {
                if (bus.choferId != null && bus.horasTrabajadasChofer > 0) {
                    EstadoChoferSemanal estadoChofer = estadoChoferesSemanal.get(bus.choferId);
                    if (estadoChofer != null) {
                        estadoChofer.registrarDia(bus.horasTrabajadasChofer);
                        log.debug("Chofer {} trabajó {:.1f}h el {} (días extendidos esta semana: {})",
                                bus.choferNombre, bus.horasTrabajadasChofer, fecha, 
                                estadoChofer.diasExtendidos);
                    }
                }
            }

            fecha = fecha.plusDays(1);
        }

        // Estadísticas
        int totalFrecuencias = frecuencias.size();
        int frecuenciasPorDia = totalDias > 0 ? totalFrecuencias / totalDias : 0;
        
        // Advertencias de capacidad
        if (frecuencias.isEmpty()) {
            advertencias.add("No se pudieron generar frecuencias con la configuración actual");
        }
        
        int busesUtilizados = (int) frecuenciasPorBus.keySet().stream().distinct().count();
        if (busesUtilizados < buses.size()) {
            advertencias.add("Solo " + busesUtilizados + " de " + buses.size() + " buses serán utilizados");
        }

        return PreviewGeneracionInteligente.builder()
                .totalFrecuencias(totalFrecuencias)
                .frecuenciasPorDia(frecuenciasPorDia)
                .diasOperacion(totalDias)
                .busesUtilizados(busesUtilizados)
                .busesDisponibles(buses.size())
                .frecuencias(frecuencias)
                .frecuenciasPorRuta(frecuenciasPorRuta)
                .frecuenciasPorBus(frecuenciasPorBus)
                .advertencias(advertencias)
                .errores(errores)
                .esViable(errores.isEmpty() && !frecuencias.isEmpty())
                .build();
        } catch (Exception e) {
            log.error("Error en preview de generación: {}", e.getMessage(), e);
            errores.add("Error interno: " + e.getMessage());
            return buildEmptyPreview(errores);
        }
    }

    /**
     * Distribuye los buses equitativamente entre los terminales de origen
     */
    private Map<Long, List<EstadoBus>> distribuirBusesEnTerminales(
            List<Bus> buses, 
            List<CooperativaTerminal> terminales,
            List<RutaCircuitoRequest> rutas,
            Map<Long, List<BusChofer>> choferesPorBus) {
        
        Map<Long, List<EstadoBus>> resultado = new HashMap<>();
        
        // Obtener terminales únicos de origen de las rutas seleccionadas
        Set<Long> terminalesOrigen = rutas.stream()
                .map(RutaCircuitoRequest::getTerminalOrigenId)
                .collect(Collectors.toSet());

        log.info("Terminales de origen requeridos: {}", terminalesOrigen);

        // Mapear terminales por ID (filtrar nulls)
        Map<Long, Terminal> terminalesMap = terminales.stream()
                .filter(ct -> ct.getTerminal() != null)
                .collect(Collectors.toMap(ct -> ct.getTerminal().getId(), CooperativaTerminal::getTerminal, 
                        (existing, replacement) -> existing));  // En caso de duplicados, mantener el primero
        
        log.info("Terminales de la cooperativa: {}", terminalesMap.keySet());

        // Si no hay terminales de origen, no hay nada que distribuir
        if (terminalesOrigen.isEmpty()) {
            log.warn("No hay terminales de origen en las rutas seleccionadas");
            return resultado;
        }
        
        // Verificar que los terminales de origen estén en la cooperativa
        Set<Long> terminalesValidos = terminalesOrigen.stream()
                .filter(terminalesMap::containsKey)
                .collect(Collectors.toSet());
        
        log.info("Terminales válidos (origen que están en cooperativa): {}", terminalesValidos);
        
        if (terminalesValidos.isEmpty()) {
            log.warn("Ningún terminal de origen {} está en la cooperativa {}", terminalesOrigen, terminalesMap.keySet());
            return resultado;
        }

        // Distribuir buses equitativamente entre terminales válidos
        int busIndex = 0;
        List<Long> listaTerminales = new ArrayList<>(terminalesValidos);
        
        log.info("Distribuyendo {} buses entre {} terminales: {}", buses.size(), listaTerminales.size(), listaTerminales);
        
        for (Bus bus : buses) {
            Long terminalId = listaTerminales.get(busIndex % listaTerminales.size());
            Terminal terminal = terminalesMap.get(terminalId);
            
            EstadoBus estadoBus = new EstadoBus(bus, terminalId, terminal != null ? terminal.getNombre() : "Terminal");
            
            log.debug("Bus {} asignado a terminal {}", bus.getPlaca(), terminalId);
            
            // Asignar el primer chofer disponible del bus
            List<BusChofer> choferes = choferesPorBus.get(bus.getId());
            if (choferes != null && !choferes.isEmpty()) {
                BusChofer asignacion = choferes.get(0);
                estadoBus.choferId = asignacion.getChofer().getId();
                estadoBus.choferNombre = asignacion.getChofer().getNombres() + " " + 
                        (asignacion.getChofer().getApellidos() != null ? asignacion.getChofer().getApellidos() : "");
            }
            
            resultado.computeIfAbsent(terminalId, k -> new ArrayList<>())
                    .add(estadoBus);
            
            busIndex++;
        }

        // Log del resultado de distribución
        for (Map.Entry<Long, List<EstadoBus>> entry : resultado.entrySet()) {
            log.info("Terminal {}: {} buses asignados", entry.getKey(), entry.getValue().size());
        }

        return resultado;
    }

    /**
     * Genera los viajes de un día completo
     */
    private void generarViajesDelDia(
            LocalDate fecha,
            String diaSemana,
            List<RutaCircuitoRequest> rutas,
            Map<Long, List<EstadoBus>> busesPorTerminal,
            LocalTime horaInicio,
            LocalTime horaFin,
            int maxHorasChofer,
            int descansoInterprov,
            int descansoIntraprov,
            int umbralKm,
            Map<Long, List<BusChofer>> choferesPorBus,
            List<FrecuenciaPreview> frecuencias,
            Map<String, Integer> frecuenciasPorRuta,
            Map<String, Integer> frecuenciasPorBus,
            List<String> advertencias,
            Boolean permitirParadas) {

        // Agrupar rutas por terminal de origen
        Map<Long, List<RutaCircuitoRequest>> rutasPorTerminalOrigen = rutas.stream()
                .collect(Collectors.groupingBy(RutaCircuitoRequest::getTerminalOrigenId));

        // Para cada terminal de origen, generar viajes con los buses disponibles
        for (Map.Entry<Long, List<RutaCircuitoRequest>> entry : rutasPorTerminalOrigen.entrySet()) {
            Long terminalOrigenId = entry.getKey();
            List<RutaCircuitoRequest> rutasDesdeTerminal = entry.getValue();
            List<EstadoBus> busesEnTerminal = busesPorTerminal.getOrDefault(terminalOrigenId, new ArrayList<>());

            log.info("Terminal origen {}: {} rutas, {} buses asignados", 
                    terminalOrigenId, rutasDesdeTerminal.size(), busesEnTerminal.size());

            if (busesEnTerminal.isEmpty()) {
                log.warn("No hay buses asignados al terminal {}. Buses por terminal: {}", 
                        terminalOrigenId, busesPorTerminal.keySet());
                continue;
            }

            // Generar viajes alternando rutas y buses
            LocalTime horaActual = horaInicio;
            int rutaIndex = 0;
            
            log.info(">>> ENTRANDO A GENERAR VIAJES: terminal={}, horaInicio={}, horaFin={}, limite={}, condicion={}", 
                    terminalOrigenId, horaInicio, horaFin, horaFin.minusHours(2), 
                    horaInicio.isBefore(horaFin.minusHours(2)));
            log.info(">>> Estado inicial de buses en terminal {}: {}", terminalOrigenId,
                    busesEnTerminal.stream()
                            .map(b -> String.format("[%s: horaDisp=%s, horasTrab=%.1f/%d]", 
                                    b.placa, b.horaDisponible, b.horasTrabajadasChofer, maxHorasChofer))
                            .collect(Collectors.joining(", ")));
            
            int iteraciones = 0;
            while (horaActual.isBefore(horaFin.minusHours(2))) { // Dejar margen para el último viaje
                iteraciones++;
                if (iteraciones == 1) {
                    log.info(">>> Primera iteración del while, horaActual={}", horaActual);
                }
                // Variable final para usar en lambdas
                final LocalTime horaActualFinal = horaActual;
                
                // Buscar un bus disponible en este terminal
                log.debug("Buscando bus disponible en hora {}, maxHorasChofer={}", horaActualFinal, maxHorasChofer);
                EstadoBus busDisponible = busesEnTerminal.stream()
                        .filter(b -> {
                            boolean disponiblePorHora = !b.horaDisponible.isAfter(horaActualFinal);
                            boolean disponiblePorHorasTrabajadas = b.horasTrabajadasChofer < maxHorasChofer;
                            log.debug("Bus {}: horaDisp={}, horasTrabajadasChofer={}, disponiblePorHora={}, disponiblePorHoras={}", 
                                    b.placa, b.horaDisponible, b.horasTrabajadasChofer, disponiblePorHora, disponiblePorHorasTrabajadas);
                            return disponiblePorHora && disponiblePorHorasTrabajadas;
                        })
                        .min(Comparator.comparing(b -> b.horaDisponible))
                        .orElse(null);

                if (busDisponible == null) {
                    log.info("No hay bus disponible en hora {}. Estado de buses: {}", 
                            horaActualFinal,
                            busesEnTerminal.stream()
                                    .map(b -> String.format("[%s: horaDisp=%s, horasTrab=%.1f/%d]", 
                                            b.placa, b.horaDisponible, b.horasTrabajadasChofer, maxHorasChofer))
                                    .collect(Collectors.joining(", ")));
                    
                    // Avanzar al próximo momento donde haya un bus disponible
                    Optional<LocalTime> proximaDisponibilidad = busesEnTerminal.stream()
                            .filter(b -> b.horasTrabajadasChofer < maxHorasChofer)
                            .map(b -> b.horaDisponible)
                            .filter(h -> h.isAfter(horaActualFinal))
                            .min(Comparator.naturalOrder());
                    
                    if (proximaDisponibilidad.isPresent()) {
                        log.info("Próxima disponibilidad: {}", proximaDisponibilidad.get());
                        horaActual = proximaDisponibilidad.get();
                        continue;
                    } else {
                        log.info("No hay más buses disponibles para el día");
                        break; // No hay más buses disponibles hoy
                    }
                }

                // Seleccionar la siguiente ruta (rotando entre las disponibles)
                RutaCircuitoRequest ruta = rutasDesdeTerminal.get(rutaIndex % rutasDesdeTerminal.size());
                rutaIndex++;

                // Calcular duración y tipo
                int duracionIda = ruta.getDuracionMinutos() != null ? ruta.getDuracionMinutos() : 120;
                boolean esInterprovincial = ruta.getDistanciaKm() != null && ruta.getDistanciaKm() > umbralKm;
                int tiempoDescanso = esInterprovincial ? descansoInterprov : descansoIntraprov;
                TipoFrecuencia tipo = esInterprovincial ? TipoFrecuencia.INTERPROVINCIAL : TipoFrecuencia.INTRAPROVINCIAL;

                // Verificar si el chofer tiene horas disponibles para ida + descanso + vuelta
                double horasViaje = (duracionIda * 2 + tiempoDescanso) / 60.0;
                if (busDisponible.horasTrabajadasChofer + horasViaje > maxHorasChofer) {
                    // Chofer no tiene suficientes horas, buscar otro bus
                    horaActual = horaActual.plusMinutes(15);
                    continue;
                }

                // Generar viaje de IDA
                LocalTime horaSalidaIda = horaActual;
                LocalTime horaLlegadaIda = horaSalidaIda.plusMinutes(duracionIda);

                // Verificar que no pase de la hora fin
                if (horaLlegadaIda.isAfter(horaFin)) {
                    break;
                }

                String rutaKey = ruta.getTerminalOrigenNombre() + " → " + ruta.getTerminalDestinoNombre();
                int maxParadas = Boolean.TRUE.equals(permitirParadas) && esInterprovincial 
                        ? calcularMaxParadasPermitidas(duracionIda, tipo) 
                        : 0;

                // Calcular costo de combustible (0.12 L/km * precio diesel ~$1.80)
                Double distanciaKm = ruta.getDistanciaKm() != null ? ruta.getDistanciaKm() : 100.0;
                Double costoCombustible = distanciaKm * 0.12 * 1.80;
                
                // Incrementar orden del día para este bus
                busDisponible.ordenDiaActual++;

                // Frecuencia de IDA
                frecuencias.add(FrecuenciaPreview.builder()
                        .fecha(fecha)
                        .diaSemana(diaSemana)
                        .horaSalida(horaSalidaIda)
                        .horaLlegada(horaLlegadaIda)
                        .terminalOrigenId(ruta.getTerminalOrigenId())
                        .terminalOrigenNombre(ruta.getTerminalOrigenNombre())
                        .terminalDestinoId(ruta.getTerminalDestinoId())
                        .terminalDestinoNombre(ruta.getTerminalDestinoNombre())
                        .busId(busDisponible.busId)
                        .busPlaca(busDisponible.placa)
                        .asientosDisponibles(busDisponible.asientosDisponibles)
                        .tipoFrecuencia(tipo.name())
                        .duracionMinutos(duracionIda)
                        .tiempoDescansoMinutos(tiempoDescanso)
                        .paradasPermitidas(maxParadas)
                        .precio(ruta.getPrecioBase())
                        .distanciaKm(distanciaKm)
                        .costoCombustibleEstimado(costoCombustible)
                        .choferId(busDisponible.choferId)
                        .choferNombre(busDisponible.choferNombre)
                        .ordenDia(busDisponible.ordenDiaActual)
                        .esViajeDe("IDA")
                        .build());

                frecuenciasPorRuta.merge(rutaKey, 1, Integer::sum);
                frecuenciasPorBus.merge(busDisponible.placa, 1, Integer::sum);

                // Actualizar estado del bus - ahora está en el terminal destino
                busDisponible.terminalActualId = ruta.getTerminalDestinoId();
                busDisponible.terminalActualNombre = ruta.getTerminalDestinoNombre();
                busDisponible.horaDisponible = horaLlegadaIda.plusMinutes(tiempoDescanso);
                busDisponible.horasTrabajadasChofer += duracionIda / 60.0;
                busDisponible.viajesHoy++;

                // Generar viaje de VUELTA (desde el destino al origen)
                LocalTime horaSalidaVuelta = busDisponible.horaDisponible;
                LocalTime horaLlegadaVuelta = horaSalidaVuelta.plusMinutes(duracionIda);

                if (!horaLlegadaVuelta.isAfter(horaFin)) {
                    String rutaVueltaKey = ruta.getTerminalDestinoNombre() + " → " + ruta.getTerminalOrigenNombre();
                    
                    // Incrementar orden del día para la vuelta
                    busDisponible.ordenDiaActual++;

                    frecuencias.add(FrecuenciaPreview.builder()
                            .fecha(fecha)
                            .diaSemana(diaSemana)
                            .horaSalida(horaSalidaVuelta)
                            .horaLlegada(horaLlegadaVuelta)
                            .terminalOrigenId(ruta.getTerminalDestinoId())
                            .terminalOrigenNombre(ruta.getTerminalDestinoNombre())
                            .terminalDestinoId(ruta.getTerminalOrigenId())
                            .terminalDestinoNombre(ruta.getTerminalOrigenNombre())
                            .busId(busDisponible.busId)
                            .busPlaca(busDisponible.placa)
                            .asientosDisponibles(busDisponible.asientosDisponibles)
                            .tipoFrecuencia(tipo.name())
                            .duracionMinutos(duracionIda)
                            .tiempoDescansoMinutos(tiempoDescanso)
                            .paradasPermitidas(maxParadas)
                            .precio(ruta.getPrecioBase())
                            .distanciaKm(distanciaKm)
                            .costoCombustibleEstimado(costoCombustible)
                            .choferId(busDisponible.choferId)
                            .choferNombre(busDisponible.choferNombre)
                            .ordenDia(busDisponible.ordenDiaActual)
                            .esViajeDe("VUELTA")
                            .build());

                    frecuenciasPorRuta.merge(rutaVueltaKey, 1, Integer::sum);
                    frecuenciasPorBus.merge(busDisponible.placa, 1, Integer::sum);

                    // Bus vuelve al terminal de origen
                    busDisponible.terminalActualId = ruta.getTerminalOrigenId();
                    busDisponible.terminalActualNombre = ruta.getTerminalOrigenNombre();
                    busDisponible.horaDisponible = horaLlegadaVuelta.plusMinutes(tiempoDescanso);
                    busDisponible.horasTrabajadasChofer += duracionIda / 60.0;
                    busDisponible.viajesHoy++;
                }

                // Avanzar tiempo para el próximo ciclo
                horaActual = horaActual.plusMinutes(30); // Siguiente slot de salida
            }
        }
    }

    /**
     * NUEVO: Genera viajes del día distribuyendo buses entre TODAS las rutas
     * Los buses rotan entre rutas en lugar de estar fijos en un terminal
     * @param maxHorasPorChofer Mapa con horas máximas personalizadas por chofer (basado en días extendidos de la semana)
     */
    private void generarViajesDelDiaV2(
            LocalDate fecha,
            String diaSemana,
            List<RutaCircuitoRequest> rutas,
            List<EstadoBus> poolBuses,
            LocalTime horaInicio,
            LocalTime horaFin,
            int maxHorasChoferDefault,
            int descansoInterprov,
            int descansoIntraprov,
            int umbralKm,
            List<FrecuenciaPreview> frecuencias,
            Map<String, Integer> frecuenciasPorRuta,
            Map<String, Integer> frecuenciasPorBus,
            List<String> advertencias,
            Boolean permitirParadas,
            Integer maxParadasConfig,
            Map<Long, Integer> maxHorasPorChofer) {

        log.info("=== Generando viajes V2 para {} con {} rutas y {} buses ===", 
                fecha, rutas.size(), poolBuses.size());

        if (rutas.isEmpty() || poolBuses.isEmpty()) {
            log.warn("No hay rutas o buses para generar viajes");
            return;
        }

        // ESTRATEGIA: Asignar buses a rutas de forma equitativa
        // Cada ruta tendrá buses asignados que harán circuitos ida-vuelta durante el día
        
        // Calcular cuántos buses asignar a cada ruta
        int busesDisponibles = poolBuses.size();
        int numRutas = rutas.size();
        
        // Distribuir buses entre rutas (al menos 1 por ruta si hay suficientes)
        Map<Integer, List<EstadoBus>> busesAsignadosPorRuta = new HashMap<>();
        int busIndex = 0;
        
        // Primera pasada: asignar al menos 1 bus por ruta
        for (int i = 0; i < numRutas && busIndex < busesDisponibles; i++) {
            List<EstadoBus> listaBuses = new ArrayList<>();
            listaBuses.add(poolBuses.get(busIndex++));
            busesAsignadosPorRuta.put(i, listaBuses);
        }
        
        // Segunda pasada: distribuir buses restantes entre las rutas
        int rutaIdx = 0;
        while (busIndex < busesDisponibles) {
            busesAsignadosPorRuta.get(rutaIdx % numRutas).add(poolBuses.get(busIndex++));
            rutaIdx++;
        }
        
        log.info("Distribución de buses por ruta:");
        for (int i = 0; i < numRutas; i++) {
            RutaCircuitoRequest ruta = rutas.get(i);
            List<EstadoBus> buses = busesAsignadosPorRuta.getOrDefault(i, new ArrayList<>());
            log.info("  Ruta {}: {} -> {} ({} buses)", i, 
                    ruta.getTerminalOrigenNombre(), ruta.getTerminalDestinoNombre(), buses.size());
        }

        // Para cada ruta, generar viajes con los buses asignados
        log.info(">>> INICIANDO LOOP DE GENERACIÓN para {} rutas", numRutas);
        for (int rutaIndex = 0; rutaIndex < numRutas; rutaIndex++) {
            RutaCircuitoRequest ruta = rutas.get(rutaIndex);
            List<EstadoBus> busesDeRuta = busesAsignadosPorRuta.getOrDefault(rutaIndex, new ArrayList<>());
            
            log.info(">>> Procesando ruta {}/{}: {} -> {} con {} buses", 
                    rutaIndex + 1, numRutas,
                    ruta.getTerminalOrigenNombre(), ruta.getTerminalDestinoNombre(),
                    busesDeRuta.size());
            
            if (busesDeRuta.isEmpty()) {
                advertencias.add("Ruta " + ruta.getTerminalOrigenNombre() + " → " + 
                        ruta.getTerminalDestinoNombre() + " sin buses asignados");
                continue;
            }
            
            // Calcular parámetros de la ruta
            int duracionIda = ruta.getDuracionMinutos() != null ? ruta.getDuracionMinutos() : 120;
            boolean esInterprovincial = ruta.getDistanciaKm() != null && ruta.getDistanciaKm() > umbralKm;
            
            // TIEMPO DE DESCANSO DINÁMICO basado en duración del viaje
            // Fórmula: 25% del tiempo de viaje, mínimo 15 min, máximo 90 min
            int tiempoDescansoCalculado = Math.max(15, Math.min(90, (int)(duracionIda * 0.25)));
            // Usar el valor calculado o el configurado si es mayor (para seguridad)
            int tiempoDescansoBase = esInterprovincial ? descansoInterprov : descansoIntraprov;
            int tiempoDescanso = Math.max(tiempoDescansoCalculado, Math.min(tiempoDescansoBase, tiempoDescansoCalculado + 15));
            
            log.info(">>> Descanso para ruta {}: calculado={}min (25% de {}min), base={}min, final={}min",
                    ruta.getTerminalOrigenNombre() + "-" + ruta.getTerminalDestinoNombre(),
                    tiempoDescansoCalculado, duracionIda, tiempoDescansoBase, tiempoDescanso);
            
            TipoFrecuencia tipo = esInterprovincial ? TipoFrecuencia.INTERPROVINCIAL : TipoFrecuencia.INTRAPROVINCIAL;
            Double distanciaKm = ruta.getDistanciaKm() != null ? ruta.getDistanciaKm() : 100.0;
            Double costoCombustible = distanciaKm * 0.12 * 1.80;
            
            // Calcular paradas permitidas
            int maxParadas = 0;
            if (Boolean.TRUE.equals(permitirParadas) && esInterprovincial) {
                maxParadas = maxParadasConfig != null ? maxParadasConfig : calcularMaxParadasPermitidas(duracionIda, tipo);
            }
            
            // Tiempo total de un circuito completo (ida + descanso + vuelta + descanso)
            int tiempoCircuitoMinutos = duracionIda * 2 + tiempoDescanso * 2;
            double horasCircuito = tiempoCircuitoMinutos / 60.0;
            double horasViajeSolo = duracionIda / 60.0; // Solo ida
            
            log.info(">>> Ruta {} ({}→{}): durIda={}min ({}h), descanso={}min, circuito={}min ({}h), maxHorasDefault={}", 
                    rutaIndex, ruta.getTerminalOrigenNombre(), ruta.getTerminalDestinoNombre(),
                    duracionIda, horasViajeSolo, tiempoDescanso, tiempoCircuitoMinutos, horasCircuito, maxHorasChoferDefault);
            
            // Verificar si la ruta es factible (al menos puede hacer ida con 10h máximo)
            if (horasViajeSolo > 10) {
                log.warn(">>> Ruta {} OMITIDA: duración ida ({}h) excede max horas posibles (10h)", 
                        rutaIndex, horasViajeSolo);
                advertencias.add("Ruta " + ruta.getTerminalOrigenNombre() + " → " + 
                        ruta.getTerminalDestinoNombre() + " omitida: duración excede horas máximas del chofer");
                continue;
            }
            
            // Generar viajes para cada bus asignado a esta ruta
            for (EstadoBus bus : busesDeRuta) {
                // Obtener max horas personalizadas para este chofer (basado en días extendidos de la semana)
                int maxHorasChoferHoy = maxHorasPorChofer != null && bus.choferId != null 
                        ? maxHorasPorChofer.getOrDefault(bus.choferId, maxHorasChoferDefault) 
                        : maxHorasChoferDefault;
                
                LocalTime horaActual = horaInicio;
                
                // Escalonar inicio de buses para evitar que salgan todos al mismo tiempo
                int offsetMinutos = busesDeRuta.indexOf(bus) * 15; // 15 min entre cada bus
                horaActual = horaActual.plusMinutes(offsetMinutos);
                
                log.info(">>> Bus {} asignado a ruta {}, inicio a las {}, horasTrabajadasChofer={}, maxHorasHoy={}", 
                        bus.placa, rutaIndex, horaActual, bus.horasTrabajadasChofer, maxHorasChoferHoy);
                
                // Generar viajes mientras el chofer tenga horas disponibles para AL MENOS ida
                // Usa maxHorasChoferHoy que considera el límite semanal de días extendidos
                while (bus.horasTrabajadasChofer + horasViajeSolo <= maxHorasChoferHoy) {
                    // Verificar que la hora actual + duración no exceda hora fin
                    LocalTime horaLlegadaIda = horaActual.plusMinutes(duracionIda);
                    if (horaLlegadaIda.isAfter(horaFin)) {
                        break;
                    }
                    
                    bus.ordenDiaActual++;
                    String rutaIdaKey = ruta.getTerminalOrigenNombre() + " → " + ruta.getTerminalDestinoNombre();
                    
                    // === VIAJE DE IDA ===
                    frecuencias.add(FrecuenciaPreview.builder()
                            .fecha(fecha)
                            .diaSemana(diaSemana)
                            .horaSalida(horaActual)
                            .horaLlegada(horaLlegadaIda)
                            .terminalOrigenId(ruta.getTerminalOrigenId())
                            .terminalOrigenNombre(ruta.getTerminalOrigenNombre())
                            .terminalDestinoId(ruta.getTerminalDestinoId())
                            .terminalDestinoNombre(ruta.getTerminalDestinoNombre())
                            .busId(bus.busId)
                            .busPlaca(bus.placa)
                            .asientosDisponibles(bus.asientosDisponibles)
                            .tipoFrecuencia(tipo.name())
                            .duracionMinutos(duracionIda)
                            .tiempoDescansoMinutos(tiempoDescanso)
                            .paradasPermitidas(maxParadas)
                            .precio(ruta.getPrecioBase())
                            .distanciaKm(distanciaKm)
                            .costoCombustibleEstimado(costoCombustible)
                            .choferId(bus.choferId)
                            .choferNombre(bus.choferNombre)
                            .ordenDia(bus.ordenDiaActual)
                            .esViajeDe("IDA")
                            .build());
                    
                    frecuenciasPorRuta.merge(rutaIdaKey, 1, Integer::sum);
                    frecuenciasPorBus.merge(bus.placa, 1, Integer::sum);
                    bus.horasTrabajadasChofer += duracionIda / 60.0;
                    bus.viajesHoy++;
                    
                    // === VIAJE DE VUELTA ===
                    LocalTime horaSalidaVuelta = horaLlegadaIda.plusMinutes(tiempoDescanso);
                    LocalTime horaLlegadaVuelta = horaSalidaVuelta.plusMinutes(duracionIda);
                    
                    // Verificar si puede hacer vuelta (usando maxHorasChoferHoy personalizado)
                    if (!horaLlegadaVuelta.isAfter(horaFin) && 
                        bus.horasTrabajadasChofer + (duracionIda / 60.0) <= maxHorasChoferHoy) {
                        
                        bus.ordenDiaActual++;
                        String rutaVueltaKey = ruta.getTerminalDestinoNombre() + " → " + ruta.getTerminalOrigenNombre();
                        
                        frecuencias.add(FrecuenciaPreview.builder()
                                .fecha(fecha)
                                .diaSemana(diaSemana)
                                .horaSalida(horaSalidaVuelta)
                                .horaLlegada(horaLlegadaVuelta)
                                .terminalOrigenId(ruta.getTerminalDestinoId())
                                .terminalOrigenNombre(ruta.getTerminalDestinoNombre())
                                .terminalDestinoId(ruta.getTerminalOrigenId())
                                .terminalDestinoNombre(ruta.getTerminalOrigenNombre())
                                .busId(bus.busId)
                                .busPlaca(bus.placa)
                                .asientosDisponibles(bus.asientosDisponibles)
                                .tipoFrecuencia(tipo.name())
                                .duracionMinutos(duracionIda)
                                .tiempoDescansoMinutos(tiempoDescanso)
                                .paradasPermitidas(maxParadas)
                                .precio(ruta.getPrecioBase())
                                .distanciaKm(distanciaKm)
                                .costoCombustibleEstimado(costoCombustible)
                                .choferId(bus.choferId)
                                .choferNombre(bus.choferNombre)
                                .ordenDia(bus.ordenDiaActual)
                                .esViajeDe("VUELTA")
                                .build());
                        
                        frecuenciasPorRuta.merge(rutaVueltaKey, 1, Integer::sum);
                        frecuenciasPorBus.merge(bus.placa, 1, Integer::sum);
                        bus.horasTrabajadasChofer += duracionIda / 60.0;
                        bus.viajesHoy++;
                        
                        // Siguiente circuito empieza después del descanso de la vuelta
                        horaActual = horaLlegadaVuelta.plusMinutes(tiempoDescanso);
                    } else {
                        // No puede hacer vuelta, terminar el día para este bus
                        break;
                    }
                }
                
                log.info(">>> Bus {} terminó con {} viajes, {} horas trabajadas", 
                        bus.placa, bus.viajesHoy, String.format("%.1f", bus.horasTrabajadasChofer));
            }
        }
        
        // Resumen por ruta
        log.info("=== RESUMEN POR RUTA ===");
        frecuenciasPorRuta.forEach((ruta, count) -> 
            log.info("   Ruta {}: {} viajes", ruta, count));
        
        // Resumen por bus
        log.info("=== RESUMEN POR BUS ===");
        frecuenciasPorBus.forEach((placa, count) -> 
            log.info("   Bus {}: {} viajes", placa, count));
        
        log.info("=== Fin generación V2: {} frecuencias totales en {} rutas distintas ===", 
                frecuencias.size(), frecuenciasPorRuta.size());
    }

    /**
     * Guarda las frecuencias generadas.
     * IMPORTANTE: Este método elimina TODAS las frecuencias existentes de la cooperativa
     * antes de generar las nuevas.
     */
    @Transactional(noRollbackFor = {IllegalArgumentException.class})
    public ResultadoGeneracionInteligente generarFrecuencias(Long cooperativaId, GenerarInteligenteRequest request) {
        log.info("Iniciando generación de frecuencias para cooperativa {}", cooperativaId);
        
        // PASO 1: Eliminar todas las frecuencias existentes de la cooperativa
        int frecuenciasEliminadas = eliminarFrecuenciasExistentes(cooperativaId);
        log.info("Eliminadas {} frecuencias existentes para cooperativa {}", frecuenciasEliminadas, cooperativaId);
        
        // PASO 2: Generar preview
        PreviewGeneracionInteligente preview;
        try {
            preview = previewGeneracion(cooperativaId, request);
        } catch (Exception e) {
            log.error("Error generando preview: {}", e.getMessage(), e);
            return ResultadoGeneracionInteligente.builder()
                    .exito(false)
                    .frecuenciasCreadas(0)
                    .mensajes(List.of("Error generando preview: " + e.getMessage()))
                    .advertencias(List.of())
                    .build();
        }
        
        if (!preview.getEsViable()) {
            return ResultadoGeneracionInteligente.builder()
                    .exito(false)
                    .frecuenciasCreadas(0)
                    .mensajes(preview.getErrores())
                    .advertencias(preview.getAdvertencias())
                    .build();
        }
        
        if (preview.getFrecuencias() == null || preview.getFrecuencias().isEmpty()) {
            return ResultadoGeneracionInteligente.builder()
                    .exito(false)
                    .frecuenciasCreadas(0)
                    .mensajes(List.of("No se generaron frecuencias en el preview"))
                    .advertencias(preview.getAdvertencias())
                    .build();
        }

        Cooperativa cooperativa = cooperativaRepository.findById(cooperativaId).orElse(null);
        if (cooperativa == null) {
            return ResultadoGeneracionInteligente.builder()
                    .exito(false)
                    .frecuenciasCreadas(0)
                    .mensajes(List.of("Cooperativa no encontrada"))
                    .advertencias(List.of())
                    .build();
        }
        
        List<String> mensajes = new ArrayList<>();
        List<String> advertencias = new ArrayList<>(preview.getAdvertencias());
        List<FrecuenciaViaje> frecuenciasAGuardar = new ArrayList<>();
        
        // IMPORTANTE: Los días de operación vienen del request, no del preview individual
        String diasOperacionCSV = (request.getDiasOperacion() != null && !request.getDiasOperacion().isEmpty())
                ? String.join(",", request.getDiasOperacion())
                : "LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,DOMINGO";
        
        // Set para rastrear combinaciones únicas (evitar duplicados en el mismo lote)
        // Agrupamos por (bus, ruta, horaSalida) ignorando el día específico
        Set<String> frecuenciasEnLote = new HashSet<>();
        int duplicadosExistentes = 0;
        int duplicadosEnLote = 0;
        
        // Cache de entidades para evitar múltiples consultas
        Map<Long, Terminal> terminalesCache = new HashMap<>();
        Map<Long, Bus> busesCache = new HashMap<>();
        Map<String, Ruta> rutasCache = new HashMap<>();

        // Fase 1: Validar y preparar todas las frecuencias
        for (FrecuenciaPreview fp : preview.getFrecuencias()) {
            try {
                // Obtener terminal origen
                Terminal terminalOrigen = terminalesCache.computeIfAbsent(
                    fp.getTerminalOrigenId(),
                    id -> terminalRepository.findById(id).orElse(null)
                );
                
                // Obtener terminal destino
                Terminal terminalDestino = terminalesCache.computeIfAbsent(
                    fp.getTerminalDestinoId(),
                    id -> terminalRepository.findById(id).orElse(null)
                );
                
                // Obtener bus
                Bus bus = busesCache.computeIfAbsent(
                    fp.getBusId(),
                    id -> busRepository.findById(id).orElse(null)
                );
                
                // Validaciones
                if (terminalOrigen == null) {
                    advertencias.add("Terminal origen ID " + fp.getTerminalOrigenId() + " no encontrado");
                    continue;
                }
                if (terminalDestino == null) {
                    advertencias.add("Terminal destino ID " + fp.getTerminalDestinoId() + " no encontrado");
                    continue;
                }
                if (bus == null) {
                    advertencias.add("Bus ID " + fp.getBusId() + " no encontrado");
                    continue;
                }
                if (fp.getHoraSalida() == null) {
                    advertencias.add("Hora de salida nula para frecuencia");
                    continue;
                }
                
                // Obtener o crear ruta
                String rutaKey = terminalOrigen.getId() + "-" + terminalDestino.getId();
                Ruta ruta = rutasCache.get(rutaKey);
                if (ruta == null) {
                    ruta = obtenerOCrearRuta(terminalOrigen, terminalDestino, cooperativa);
                    if (ruta != null) {
                        rutasCache.put(rutaKey, ruta);
                    }
                }
                
                if (ruta == null) {
                    advertencias.add("No se pudo crear ruta de " + terminalOrigen.getNombre() + " a " + terminalDestino.getNombre());
                    continue;
                }
                
                // Verificar duplicados en BD
                if (frecuenciaViajeRepository.existsByBusIdAndRutaIdAndHoraSalidaAndActivoTrue(
                        bus.getId(), ruta.getId(), fp.getHoraSalida())) {
                    duplicadosExistentes++;
                    continue;
                }
                
                // Verificar duplicados en el lote actual
                String claveUnica = bus.getId() + "-" + ruta.getId() + "-" + fp.getHoraSalida();
                if (frecuenciasEnLote.contains(claveUnica)) {
                    duplicadosEnLote++;
                    continue;
                }
                frecuenciasEnLote.add(claveUnica);
                
                // Obtener camino principal de la ruta
                Camino caminoPrincipal = null;
                List<Camino> caminos = caminoRepository.findActiveByRutaIdOrdered(ruta.getId());
                if (!caminos.isEmpty()) {
                    // Preferir RAPIDO, luego NORMAL
                    caminoPrincipal = caminos.stream()
                            .filter(c -> c.getTipo() == Camino.TipoCamino.RAPIDO)
                            .findFirst()
                            .orElse(caminos.stream()
                                    .filter(c -> c.getTipo() == Camino.TipoCamino.NORMAL)
                                    .findFirst()
                                    .orElse(caminos.get(0)));
                }
                
                // Obtener chofer si está asignado
                com.andinobus.backendsmartcode.cooperativa.domain.entities.UsuarioCooperativa chofer = null;
                if (fp.getChoferId() != null) {
                    chofer = usuarioCooperativaRepository.findById(fp.getChoferId()).orElse(null);
                }
                
                // Calcular valores por defecto
                Integer asientos = fp.getAsientosDisponibles() != null ? fp.getAsientosDisponibles() : 
                        (bus.getCapacidadAsientos() != null ? bus.getCapacidadAsientos() : 40);
                Integer duracionMinutos = fp.getDuracionMinutos() != null ? fp.getDuracionMinutos() : 120;
                Double kilometros = fp.getDistanciaKm() != null ? fp.getDistanciaKm() : 
                        (caminoPrincipal != null ? caminoPrincipal.getDistanciaKm() : 100.0);
                Double costoCombustible = fp.getCostoCombustibleEstimado() != null ? fp.getCostoCombustibleEstimado() :
                        (kilometros * 0.12 * 1.80);  // 0.12 L/km * $1.80/L
                
                // Construir frecuencia con TODOS los campos
                FrecuenciaViaje frecuencia = FrecuenciaViaje.builder()
                        .bus(bus)
                        .ruta(ruta)
                        .cooperativa(cooperativa)
                        .chofer(chofer)
                        .camino(caminoPrincipal)
                        .terminalOrigen(terminalOrigen)
                        .terminalDestino(terminalDestino)
                        .horaSalida(fp.getHoraSalida())
                        .horaLlegadaEstimada(fp.getHoraLlegada() != null ? fp.getHoraLlegada() : fp.getHoraSalida().plusHours(2))
                        .precioBase(fp.getPrecio() != null ? fp.getPrecio() : 5.0)
                        .diasOperacion(diasOperacionCSV)  // Usar los días del request, no el día individual
                        .asientosDisponibles(asientos)
                        .duracionEstimadaMinutos(duracionMinutos)
                        .kilometrosRuta(kilometros)
                        .costoCombustibleEstimado(costoCombustible)
                        .ordenDia(fp.getOrdenDia())
                        .tipoFrecuencia(fp.getTipoFrecuencia() != null ? TipoFrecuencia.valueOf(fp.getTipoFrecuencia()) : TipoFrecuencia.INTERPROVINCIAL)
                        .tiempoMinimoEsperaMinutos(fp.getTiempoDescansoMinutos() != null ? fp.getTiempoDescansoMinutos() : 45)
                        .estado("ACTIVA")
                        .activo(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                
                frecuenciasAGuardar.add(frecuencia);
            } catch (Exception e) {
                log.error("Error preparando frecuencia: {}", e.getMessage(), e);
                advertencias.add("Error preparando frecuencia: " + e.getMessage());
            }
        }

        // Fase 2: Guardar todas las frecuencias en lotes
        int creadas = 0;
        int errores = 0;
        if (!frecuenciasAGuardar.isEmpty()) {
            log.info("Intentando guardar {} frecuencias", frecuenciasAGuardar.size());
            
            // Guardar en lotes de 50 para mejor rendimiento
            int batchSize = 50;
            for (int i = 0; i < frecuenciasAGuardar.size(); i += batchSize) {
                int end = Math.min(i + batchSize, frecuenciasAGuardar.size());
                List<FrecuenciaViaje> batch = frecuenciasAGuardar.subList(i, end);
                
                try {
                    for (FrecuenciaViaje frecuencia : batch) {
                        entityManager.persist(frecuencia);
                        creadas++;
                    }
                    entityManager.flush();
                    entityManager.clear();
                    log.info("Lote guardado: {} de {}", end, frecuenciasAGuardar.size());
                } catch (Exception e) {
                    log.error("Error guardando lote de frecuencias: {}", e.getMessage(), e);
                    errores += batch.size();
                    advertencias.add("Error guardando lote: " + e.getMessage());
                    // Limpiar el entity manager para poder continuar
                    entityManager.clear();
                }
            }
            log.info("Se guardaron {} frecuencias, {} errores", creadas, errores);
        }

        mensajes.add("Se crearon " + creadas + " frecuencias exitosamente");
        if (duplicadosExistentes > 0) {
            mensajes.add("Se omitieron " + duplicadosExistentes + " frecuencias que ya existían en la base de datos");
        }
        if (duplicadosEnLote > 0) {
            mensajes.add("Se omitieron " + duplicadosEnLote + " frecuencias duplicadas en el mismo lote");
        }
        if (errores > 0) {
            advertencias.add("Hubo " + errores + " errores al guardar frecuencias");
        }

        return ResultadoGeneracionInteligente.builder()
                .exito(creadas > 0)
                .frecuenciasCreadas(creadas)
                .mensajes(mensajes)
                .advertencias(advertencias)
                .build();
    }

    /**
     * Obtiene una ruta existente o crea una nueva para la combinación origen-destino
     */
    private Ruta obtenerOCrearRuta(Terminal origen, Terminal destino, Cooperativa cooperativa) {
        if (origen == null || destino == null) {
            log.warn("Terminal origen o destino es null");
            return null;
        }
        
        try {
            // Buscar ruta existente
            Ruta ruta = rutaRepository.findByTerminalOrigenAndTerminalDestino(origen, destino).orElse(null);
            
            if (ruta == null) {
                log.info("Creando nueva ruta: {} -> {}", origen.getNombre(), destino.getNombre());
                // Crear nueva ruta
                String nombreRuta = origen.getNombre() + " - " + destino.getNombre();
                double distancia = calcularDistanciaKm(origen, destino);
                ruta = Ruta.builder()
                        .nombre(nombreRuta)
                        .origen(origen.getNombre())
                        .destino(destino.getNombre())
                        .terminalOrigen(origen)
                        .terminalDestino(destino)
                        .distanciaKm(distancia)
                        .duracionEstimadaMinutos(calcularDuracionMinutos(distancia))
                        .activo(true)
                        .aprobadaAnt(false)
                        .tipoRuta(origen.getProvincia() != null && destino.getProvincia() != null 
                                  && origen.getProvincia().equalsIgnoreCase(destino.getProvincia()) 
                                  ? "INTRAPROVINCIAL" : "INTERPROVINCIAL")
                        .build();
                ruta = rutaRepository.save(ruta);
                log.info("Ruta creada con ID: {}", ruta.getId());
            }
            
            return ruta;
        } catch (Exception e) {
            log.error("Error obteniendo/creando ruta: {}", e.getMessage(), e);
            return null;
        }
    }

    // ============ Métodos auxiliares ============

    private double calcularDistanciaKm(Terminal origen, Terminal destino) {
        if (origen.getLatitud() == null || origen.getLongitud() == null ||
            destino.getLatitud() == null || destino.getLongitud() == null) {
            return 150.0; // Distancia por defecto si no hay coordenadas
        }
        
        double lat1 = origen.getLatitud();
        double lon1 = origen.getLongitud();
        double lat2 = destino.getLatitud();
        double lon2 = destino.getLongitud();
        
        // Fórmula Haversine
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        
        return R * c * 1.3; // Factor de corrección por carretera
    }

    private int calcularDuracionMinutos(double distanciaKm) {
        // Velocidad promedio ~50 km/h considerando paradas y tráfico
        return (int) (distanciaKm / 50.0 * 60);
    }

    private double calcularPrecioSugerido(double distanciaKm) {
        // ~$0.04 por km
        return Math.round(distanciaKm * 0.04 * 100.0) / 100.0;
    }

    /**
     * Calcula máximo de paradas permitidas según duración y tipo
     * - Intraprovincial: 0 paradas
     * - Interprovincial 1-3h: 1 parada
     * - Interprovincial 4-6h: 2 paradas
     * - Interprovincial >8h: 3 paradas
     */
    private int calcularMaxParadasPermitidas(int duracionMinutos, TipoFrecuencia tipo) {
        if (tipo == TipoFrecuencia.INTRAPROVINCIAL) {
            return 0;
        }
        
        int horas = duracionMinutos / 60;
        if (horas <= 3) return 1;
        if (horas <= 6) return 2;
        return 3;
    }

    private String obtenerDiaSemana(LocalDate fecha) {
        return fecha.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("es", "ES"))
                .toUpperCase()
                .replace("MIÉRCOLES", "MIERCOLES")
                .replace("SÁBADO", "SABADO");
    }

    private PreviewGeneracionInteligente buildEmptyPreview(List<String> errores) {
        return PreviewGeneracionInteligente.builder()
                .totalFrecuencias(0)
                .frecuenciasPorDia(0)
                .diasOperacion(0)
                .busesUtilizados(0)
                .busesDisponibles(0)
                .frecuencias(Collections.emptyList())
                .frecuenciasPorRuta(Collections.emptyMap())
                .frecuenciasPorBus(Collections.emptyMap())
                .advertencias(Collections.emptyList())
                .errores(errores)
                .esViable(false)
                .build();
    }

    /**
     * Elimina físicamente todas las frecuencias existentes de la cooperativa.
     * Primero elimina las referencias en disponibilidad_bus para evitar violación de FK.
     */
    private int eliminarFrecuenciasExistentes(Long cooperativaId) {
        // Primero eliminar todas las referencias en disponibilidad_bus
        disponibilidadBusRepository.deleteByCooperativaId(cooperativaId);
        
        // Obtener todas las frecuencias de buses de esta cooperativa
        List<FrecuenciaViaje> frecuenciasAEliminar = frecuenciaViajeRepository.findAll().stream()
                .filter(f -> f.getBus() != null 
                        && f.getBus().getCooperativa() != null 
                        && f.getBus().getCooperativa().getId().equals(cooperativaId))
                .toList();
        
        int cantidad = frecuenciasAEliminar.size();
        
        if (cantidad > 0) {
            log.info("Eliminando {} frecuencias para cooperativa {}", cantidad, cooperativaId);
            // Eliminar físicamente (las paradas se eliminan por CASCADE)
            frecuenciaViajeRepository.deleteAll(frecuenciasAEliminar);
        }
        
        return cantidad;
    }
}
