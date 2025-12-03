package com.andinobus.backendsmartcode.cooperativa.application.services;

import com.andinobus.backendsmartcode.admin.domain.entities.FrecuenciaViaje;
import com.andinobus.backendsmartcode.admin.domain.entities.Ruta;
import com.andinobus.backendsmartcode.admin.domain.enums.TipoFrecuencia;
import com.andinobus.backendsmartcode.admin.domain.repositories.FrecuenciaViajeRepository;
import com.andinobus.backendsmartcode.admin.domain.repositories.RutaRepository;
import com.andinobus.backendsmartcode.catalogos.domain.entities.*;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.CooperativaTerminalRepository;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.TerminalRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.*;
import com.andinobus.backendsmartcode.cooperativa.api.dto.GeneracionAutomaticaDtos.*;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.FrecuenciaConfigCooperativa;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.UsuarioCooperativa;
import com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories.FrecuenciaConfigCooperativaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para la generación automática de frecuencias basada en reglas de negocio.
 * Este servicio genera frecuencias para las rutas entre terminales de la cooperativa,
 * asignando buses y choferes respetando las reglas de horas de trabajo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeneracionAutomaticaService {

    private final CooperativaTerminalRepository cooperativaTerminalRepository;
    private final TerminalRepository terminalRepository;
    private final BusRepository busRepository;
    private final BusChoferRepository busChoferRepository;
    private final FrecuenciaConfigCooperativaRepository configRepository;
    private final FrecuenciaViajeRepository frecuenciaViajeRepository;
    private final RutaRepository rutaRepository;
    private final CooperativaRepository cooperativaRepository;

    // Mapa para tracking de horas de choferes durante la generación
    private Map<Long, Double> horasChoferDia = new HashMap<>();
    private Map<Long, Set<LocalDate>> diasExcepcionalesChofer = new HashMap<>();
    
    // Mapa para tracking de frecuencias de buses durante la generación
    private Map<Long, Integer> frecuenciasBusDia = new HashMap<>();

    /**
     * Obtiene el estado actual para generación de frecuencias
     */
    @Transactional(readOnly = true)
    public EstadoGeneracion getEstadoGeneracion(Long cooperativaId) {
        // Buses de la cooperativa
        List<Bus> buses = busRepository.findByCooperativaIdAndActivoTrue(cooperativaId);
        long busesDisponibles = buses.stream()
                .filter(b -> "DISPONIBLE".equals(b.getEstado()))
                .count();

        // Choferes asignados a buses
        List<BusChofer> asignaciones = busChoferRepository.findByCooperativaId(cooperativaId);
        Set<Long> choferIds = asignaciones.stream()
                .map(bc -> bc.getChofer().getId())
                .collect(Collectors.toSet());

        // Terminales de la cooperativa
        List<CooperativaTerminal> terminales = cooperativaTerminalRepository
                .findByCooperativaIdWithTerminal(cooperativaId);

        // Generar rutas disponibles
        List<RutaDisponible> rutasDisponibles = new ArrayList<>();
        for (CooperativaTerminal origen : terminales) {
            for (CooperativaTerminal destino : terminales) {
                if (!origen.getTerminal().getId().equals(destino.getTerminal().getId())) {
                    Terminal tOrigen = origen.getTerminal();
                    Terminal tDestino = destino.getTerminal();
                    
                    double distancia = calcularDistanciaAproximada(tOrigen, tDestino);
                    int duracion = (int) (distancia * 1.2); // ~50 km/h promedio
                    double precio = distancia * 0.05; // $0.05 por km base

                    rutasDisponibles.add(RutaDisponible.builder()
                            .terminalOrigenId(tOrigen.getId())
                            .terminalOrigenNombre(tOrigen.getNombre())
                            .terminalDestinoId(tDestino.getId())
                            .terminalDestinoNombre(tDestino.getNombre())
                            .distanciaKm(Math.round(distancia * 100.0) / 100.0)
                            .duracionEstimadaMinutos(duracion)
                            .precioSugerido(Math.round(precio * 100.0) / 100.0)
                            .build());
                }
            }
        }

        // Configuración
        var config = configRepository.findByCooperativaId(cooperativaId).orElse(null);
        ConfiguracionActual configActual = ConfiguracionActual.builder()
                .maxHorasChofer(config != null ? config.getMaxHorasDiariasChofer() : 8)
                .maxHorasExcepcionales(config != null ? config.getMaxHorasExcepcionales() : 10)
                .maxDiasExcepcionales(config != null ? config.getMaxDiasExcepcionalesSemana() : 2)
                .tiempoDescansoMinutos(config != null ? config.getTiempoDescansoEntreViajesMinutos() : 30)
                .intervaloMinimoFrecuencias(config != null ? config.getIntervaloMinimoFrecuenciasMinutos() : 30)
                .horaInicio(config != null ? config.getHoraInicioOperacion().toString() : "05:00")
                .horaFin(config != null ? config.getHoraFinOperacion().toString() : "23:00")
                .build();

        return EstadoGeneracion.builder()
                .busesTotales(buses.size())
                .busesDisponibles((int) busesDisponibles)
                .choferesTotales(choferIds.size())
                .choferesDisponibles(choferIds.size()) // Simplificado
                .rutasDisponibles(rutasDisponibles)
                .configuracion(configActual)
                .build();
    }

    /**
     * Genera preview de las frecuencias a crear
     */
    @Transactional(readOnly = true)
    public PreviewAutomaticoResponse previewGeneracion(Long cooperativaId, GenerarAutomaticoRequest request) {
        List<FrecuenciaPrevisualizacion> frecuencias = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        // Obtener buses disponibles
        List<Bus> buses = busRepository.findByCooperativaIdAndActivoTrue(cooperativaId)
                .stream()
                .filter(b -> "DISPONIBLE".equals(b.getEstado()))
                .collect(Collectors.toList());

        if (buses.isEmpty()) {
            errores.add("No hay buses disponibles en la cooperativa");
            return buildEmptyPreview(errores);
        }

        // Determinar las rutas a generar
        List<RutaSeleccionada> rutasAGenerar = obtenerRutasAGenerar(cooperativaId, request);
        
        if (rutasAGenerar.isEmpty()) {
            errores.add("No hay rutas disponibles para generar frecuencias");
            return buildEmptyPreview(errores);
        }

        // Obtener configuración
        var config = configRepository.findByCooperativaId(cooperativaId).orElse(null);
        int maxHorasChofer = config != null ? config.getMaxHorasDiariasChofer() : 8;

        // Reiniciar tracking
        horasChoferDia.clear();
        diasExcepcionalesChofer.clear();
        frecuenciasBusDia.clear();

        int busIndex = 0;
        int diasOperacion = 0;
        Set<LocalDate> diasConOperacion = new HashSet<>();

        // Generar preview para cada ruta
        for (RutaSeleccionada rutaSelec : rutasAGenerar) {
            Terminal origen = terminalRepository.findById(rutaSelec.getTerminalOrigenId()).orElse(null);
            Terminal destino = terminalRepository.findById(rutaSelec.getTerminalDestinoId()).orElse(null);

            if (origen == null || destino == null) {
                advertencias.add("Ruta inválida: terminal no encontrado");
                continue;
            }

            int duracionViaje = rutaSelec.getDuracionMinutos() != null ? rutaSelec.getDuracionMinutos() : 
                    (request.getDuracionViajeMinutos() != null ? request.getDuracionViajeMinutos() : 120);
            Double precioRuta = rutaSelec.getPrecioBase() != null ? rutaSelec.getPrecioBase() : request.getPrecioBase();

            // Generar frecuencias para fechas
            LocalDate fecha = request.getFechaInicio();
            
            while (!fecha.isAfter(request.getFechaFin())) {
                String diaSemana = fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")).toUpperCase();
                diaSemana = normalizarDiaSemana(diaSemana);
                
                // Verificar si opera este día
                if (request.getDiasOperacion() != null && !request.getDiasOperacion().isEmpty() 
                        && !request.getDiasOperacion().contains(diaSemana)) {
                    fecha = fecha.plusDays(1);
                    continue;
                }

                diasConOperacion.add(fecha);
                LocalTime horaSalida = request.getHoraInicio();

                while (!horaSalida.isAfter(request.getHoraFin().minusMinutes(duracionViaje))) {
                    Bus bus = buses.get(busIndex % buses.size());
                    busIndex++;

                    BusChofer choferAsignacion = null;
                    if (Boolean.TRUE.equals(request.getAsignarChoferesAutomaticamente())) {
                        choferAsignacion = buscarChoferDisponible(bus.getId(), fecha, duracionViaje / 60.0, maxHorasChofer);
                    }

                    String estado = "OK";
                    if (choferAsignacion == null && Boolean.TRUE.equals(request.getAsignarChoferesAutomaticamente())) {
                        estado = "SIN_CHOFER";
                    }

                    frecuencias.add(FrecuenciaPrevisualizacion.builder()
                            .fecha(fecha)
                            .diaSemana(diaSemana)
                            .horaSalida(horaSalida)
                            .horaLlegada(horaSalida.plusMinutes(duracionViaje))
                            .origen(origen.getNombre())
                            .destino(destino.getNombre())
                            .busId(bus.getId())
                            .busPlaca(bus.getPlaca())
                            .choferId(choferAsignacion != null ? choferAsignacion.getChofer().getId() : null)
                            .choferNombre(choferAsignacion != null ? 
                                    choferAsignacion.getChofer().getNombres() + " " + choferAsignacion.getChofer().getApellidos() : null)
                            .precio(precioRuta)
                            .estado(estado)
                            .build());

                    horaSalida = horaSalida.plusMinutes(request.getIntervaloMinutos());
                }

                fecha = fecha.plusDays(1);
            }
        }

        diasOperacion = diasConOperacion.size();
        int frecuenciasPorDia = frecuencias.size() / Math.max(1, diasOperacion);

        if (rutasAGenerar.size() > 1) {
            advertencias.add("Se generarán frecuencias para " + rutasAGenerar.size() + " rutas");
        }

        return PreviewAutomaticoResponse.builder()
                .totalFrecuencias(frecuencias.size())
                .frecuenciasPorDia(frecuenciasPorDia)
                .diasOperacion(diasOperacion)
                .busesNecesarios(Math.min(buses.size(), frecuenciasPorDia))
                .busesDisponibles(buses.size())
                .tieneCapacidadSuficiente(errores.isEmpty())
                .frecuencias(frecuencias)
                .advertencias(advertencias)
                .errores(errores)
                .build();
    }

    /**
     * Genera las frecuencias y las guarda en la base de datos
     */
    @Transactional
    public ResultadoGeneracionAutomatica generarFrecuencias(Long cooperativaId, GenerarAutomaticoRequest request) {
        List<FrecuenciaCreada> frecuenciasCreadas = new ArrayList<>();
        List<String> mensajes = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();
        int errores = 0;
        int conAdvertencias = 0;
        int omitidas = 0;

        Cooperativa cooperativa = cooperativaRepository.findById(cooperativaId)
                .orElseThrow(() -> new EntityNotFoundException("Cooperativa no encontrada"));

        // Obtener buses disponibles
        List<Bus> buses = busRepository.findByCooperativaIdAndActivoTrue(cooperativaId)
                .stream()
                .filter(b -> "DISPONIBLE".equals(b.getEstado()))
                .collect(Collectors.toList());

        if (buses.isEmpty()) {
            mensajes.add("No hay buses disponibles para generar frecuencias");
            return ResultadoGeneracionAutomatica.builder()
                    .frecuenciasCreadas(0)
                    .frecuenciasConAdvertencias(0)
                    .errores(0)
                    .frecuenciasGeneradas(frecuenciasCreadas)
                    .mensajes(mensajes)
                    .advertencias(advertencias)
                    .build();
        }

        var config = configRepository.findByCooperativaId(cooperativaId).orElse(null);

        // Días de operación
        String diasOperacionStr = String.join(",", request.getDiasOperacion() != null && !request.getDiasOperacion().isEmpty() 
                ? request.getDiasOperacion() 
                : Arrays.asList("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO"));

        // Determinar las rutas a generar
        List<RutaSeleccionada> rutasAGenerar = obtenerRutasAGenerar(cooperativaId, request);
        
        if (rutasAGenerar.isEmpty()) {
            mensajes.add("No hay rutas disponibles para generar frecuencias");
            return ResultadoGeneracionAutomatica.builder()
                    .frecuenciasCreadas(0)
                    .frecuenciasConAdvertencias(0)
                    .errores(0)
                    .frecuenciasGeneradas(frecuenciasCreadas)
                    .mensajes(mensajes)
                    .advertencias(advertencias)
                    .build();
        }

        mensajes.add("Generando frecuencias para " + rutasAGenerar.size() + " ruta(s)");
        int busIndex = 0;

        // Generar frecuencias para cada ruta
        for (RutaSeleccionada rutaSelec : rutasAGenerar) {
            Terminal origen = terminalRepository.findById(rutaSelec.getTerminalOrigenId())
                    .orElse(null);
            Terminal destino = terminalRepository.findById(rutaSelec.getTerminalDestinoId())
                    .orElse(null);

            if (origen == null || destino == null) {
                advertencias.add("Ruta inválida: terminal origen o destino no encontrado");
                errores++;
                continue;
            }

            // Buscar o crear ruta
            String rutaNombre = origen.getNombre() + " - " + destino.getNombre();
            Ruta ruta = rutaRepository.findByNombre(rutaNombre).orElseGet(() -> {
                int duracion = rutaSelec.getDuracionMinutos() != null ? rutaSelec.getDuracionMinutos() : 
                        (request.getDuracionViajeMinutos() != null ? request.getDuracionViajeMinutos() : 120);
                Ruta nuevaRuta = Ruta.builder()
                        .nombre(rutaNombre)
                        .origen(origen.getCanton())
                        .destino(destino.getCanton())
                        .terminalOrigen(origen)
                        .terminalDestino(destino)
                        .distanciaKm(calcularDistanciaAproximada(origen, destino))
                        .duracionEstimadaMinutos(duracion)
                        .activo(true)
                        .build();
                return rutaRepository.save(nuevaRuta);
            });

            int duracionViaje = rutaSelec.getDuracionMinutos() != null ? rutaSelec.getDuracionMinutos() : 
                    (request.getDuracionViajeMinutos() != null ? request.getDuracionViajeMinutos() : 120);
            Double precioRuta = rutaSelec.getPrecioBase() != null ? rutaSelec.getPrecioBase() : request.getPrecioBase();

            // Generar frecuencias para esta ruta
            LocalTime horaSalida = request.getHoraInicio();

            while (!horaSalida.isAfter(request.getHoraFin().minusMinutes(duracionViaje))) {
                // Rotar buses
                Bus bus = buses.get(busIndex % buses.size());
                busIndex++;

                // Verificar si ya existe esta frecuencia
                boolean existe = frecuenciaViajeRepository.existsByBusIdAndRutaIdAndHoraSalidaAndActivoTrue(
                        bus.getId(), ruta.getId(), horaSalida);

                if (existe) {
                    omitidas++;
                    horaSalida = horaSalida.plusMinutes(request.getIntervaloMinutos());
                    continue;
                }

                // Buscar chofer del bus
                BusChofer choferAsignacion = null;
                if (Boolean.TRUE.equals(request.getAsignarChoferesAutomaticamente())) {
                    List<BusChofer> choferesBus = busChoferRepository.findByBusIdAndActivoTrueOrderByOrdenAsc(bus.getId());
                    if (!choferesBus.isEmpty()) {
                        choferAsignacion = choferesBus.get(0);
                    }
                }

                try {
                    // Determinar tipo de frecuencia basado en la distancia
                    TipoFrecuencia tipoFrecuencia = determinarTipoFrecuencia(ruta, config);
                    
                    // Calcular tiempo mínimo de espera según tipo
                    int tiempoMinimoEspera = tipoFrecuencia == TipoFrecuencia.INTERPROVINCIAL 
                        ? (config != null ? config.getDescansoInterprovincialMinutos() : 120)
                        : (config != null ? config.getDescansoIntraprovincialMinutos() : 45);

                    FrecuenciaViaje frecuencia = FrecuenciaViaje.builder()
                            .bus(bus)
                            .ruta(ruta)
                            .cooperativa(cooperativa)
                            .terminalOrigen(origen)
                            .terminalDestino(destino)
                            .horaSalida(horaSalida)
                            .horaLlegadaEstimada(horaSalida.plusMinutes(duracionViaje))
                            .diasOperacion(diasOperacionStr)
                            .precioBase(precioRuta)
                            .asientosDisponibles(bus.getCapacidadAsientos())
                            .tipoFrecuencia(tipoFrecuencia)
                            .tiempoMinimoEsperaMinutos(tiempoMinimoEspera)
                            .requiereBusEnTerminal(true) // Por defecto, requiere bus en terminal
                            .estado("ACTIVA")
                            .activo(true)
                            .build();

                    if (choferAsignacion != null) {
                        frecuencia.setChofer(choferAsignacion.getChofer());
                    } else if (Boolean.TRUE.equals(request.getAsignarChoferesAutomaticamente())) {
                        conAdvertencias++;
                    }

                    FrecuenciaViaje savedFrecuencia = frecuenciaViajeRepository.save(frecuencia);

                    frecuenciasCreadas.add(FrecuenciaCreada.builder()
                            .frecuenciaId(savedFrecuencia.getId())
                            .fecha(request.getFechaInicio())
                            .horaSalida(horaSalida)
                            .ruta(rutaNombre)
                            .busPlaca(bus.getPlaca())
                            .choferNombre(choferAsignacion != null ? 
                                    choferAsignacion.getChofer().getNombres() + " " + choferAsignacion.getChofer().getApellidos() : null)
                            .precio(precioRuta)
                            .build());

                } catch (Exception e) {
                    log.error("Error creando frecuencia para ruta {} hora {}: {}", rutaNombre, horaSalida, e.getMessage());
                    errores++;
                }

                horaSalida = horaSalida.plusMinutes(request.getIntervaloMinutos());
            }
        }

        mensajes.add("Proceso completado");
        mensajes.add("Frecuencias creadas: " + frecuenciasCreadas.size());
        if (omitidas > 0) {
            mensajes.add("Frecuencias omitidas (ya existían): " + omitidas);
        }

        return ResultadoGeneracionAutomatica.builder()
                .frecuenciasCreadas(frecuenciasCreadas.size())
                .frecuenciasConAdvertencias(conAdvertencias)
                .errores(errores)
                .frecuenciasGeneradas(frecuenciasCreadas)
                .mensajes(mensajes)
                .advertencias(advertencias)
                .build();
    }

    /**
     * Determina las rutas a generar según el request
     * VALIDACIÓN: Solo permite terminales asignados a la cooperativa
     */
    private List<RutaSeleccionada> obtenerRutasAGenerar(Long cooperativaId, GenerarAutomaticoRequest request) {
        // Obtener terminales asignados a la cooperativa para validación
        Set<Long> terminalesAutorizados = cooperativaTerminalRepository
                .findByCooperativaIdWithTerminal(cooperativaId)
                .stream()
                .map(ct -> ct.getTerminal().getId())
                .collect(Collectors.toSet());

        if (terminalesAutorizados.isEmpty()) {
            throw new IllegalArgumentException("La cooperativa no tiene terminales asignados. Asigne terminales antes de generar frecuencias.");
        }

        // Si se especificaron rutas seleccionadas, validar que los terminales pertenezcan a la cooperativa
        if (request.getRutasSeleccionadas() != null && !request.getRutasSeleccionadas().isEmpty()) {
            for (RutaSeleccionada ruta : request.getRutasSeleccionadas()) {
                if (!terminalesAutorizados.contains(ruta.getTerminalOrigenId())) {
                    Terminal t = terminalRepository.findById(ruta.getTerminalOrigenId()).orElse(null);
                    String nombreTerminal = t != null ? t.getNombre() : "ID: " + ruta.getTerminalOrigenId();
                    throw new IllegalArgumentException("Terminal origen '" + nombreTerminal + "' no está asignado a esta cooperativa");
                }
                if (!terminalesAutorizados.contains(ruta.getTerminalDestinoId())) {
                    Terminal t = terminalRepository.findById(ruta.getTerminalDestinoId()).orElse(null);
                    String nombreTerminal = t != null ? t.getNombre() : "ID: " + ruta.getTerminalDestinoId();
                    throw new IllegalArgumentException("Terminal destino '" + nombreTerminal + "' no está asignado a esta cooperativa");
                }
            }
            return request.getRutasSeleccionadas();
        }

        // Si se especificó origen y destino específicos (modo antiguo, una sola ruta)
        if (request.getTerminalOrigenId() != null && request.getTerminalDestinoId() != null 
                && !Boolean.TRUE.equals(request.getGenerarTodasLasRutas())) {
            // Validar que los terminales pertenezcan a la cooperativa
            if (!terminalesAutorizados.contains(request.getTerminalOrigenId())) {
                Terminal t = terminalRepository.findById(request.getTerminalOrigenId()).orElse(null);
                String nombreTerminal = t != null ? t.getNombre() : "ID: " + request.getTerminalOrigenId();
                throw new IllegalArgumentException("Terminal origen '" + nombreTerminal + "' no está asignado a esta cooperativa");
            }
            if (!terminalesAutorizados.contains(request.getTerminalDestinoId())) {
                Terminal t = terminalRepository.findById(request.getTerminalDestinoId()).orElse(null);
                String nombreTerminal = t != null ? t.getNombre() : "ID: " + request.getTerminalDestinoId();
                throw new IllegalArgumentException("Terminal destino '" + nombreTerminal + "' no está asignado a esta cooperativa");
            }
            
            return Collections.singletonList(RutaSeleccionada.builder()
                    .terminalOrigenId(request.getTerminalOrigenId())
                    .terminalDestinoId(request.getTerminalDestinoId())
                    .precioBase(request.getPrecioBase())
                    .duracionMinutos(request.getDuracionViajeMinutos())
                    .build());
        }

        // Generar todas las rutas posibles entre terminales de la cooperativa
        List<CooperativaTerminal> terminales = cooperativaTerminalRepository
                .findByCooperativaIdWithTerminal(cooperativaId);

        List<RutaSeleccionada> todasLasRutas = new ArrayList<>();
        for (CooperativaTerminal origen : terminales) {
            for (CooperativaTerminal destino : terminales) {
                if (!origen.getTerminal().getId().equals(destino.getTerminal().getId())) {
                    Terminal tOrigen = origen.getTerminal();
                    Terminal tDestino = destino.getTerminal();
                    
                    double distancia = calcularDistanciaAproximada(tOrigen, tDestino);
                    int duracion = (int) (distancia * 1.2); // ~50 km/h promedio
                    double precio = request.getPrecioBase() != null ? request.getPrecioBase() : distancia * 0.05;

                    todasLasRutas.add(RutaSeleccionada.builder()
                            .terminalOrigenId(tOrigen.getId())
                            .terminalDestinoId(tDestino.getId())
                            .precioBase(precio)
                            .duracionMinutos(duracion)
                            .build());
                }
            }
        }
        return todasLasRutas;
    }

    // === Métodos auxiliares ===

    private BusChofer buscarChoferDisponible(Long busId, LocalDate fecha, double horasNecesarias, int maxHoras) {
        List<BusChofer> choferes = busChoferRepository.findByBusIdAndActivoTrueOrderByOrdenAsc(busId);
        
        for (BusChofer bc : choferes) {
            Long choferId = bc.getChofer().getId();
            double horasUsadas = horasChoferDia.getOrDefault(choferId, 0.0);
            
            if (horasUsadas + horasNecesarias <= maxHoras) {
                return bc;
            }
        }
        
        return null;
    }

    private void actualizarHorasChofer(Long choferId, LocalDate fecha, double horas) {
        String key = choferId + "_" + fecha.toString();
        double horasActuales = horasChoferDia.getOrDefault(choferId, 0.0);
        horasChoferDia.put(choferId, horasActuales + horas);
    }

    private PreviewAutomaticoResponse buildEmptyPreview(List<String> errores) {
        return PreviewAutomaticoResponse.builder()
                .totalFrecuencias(0)
                .frecuenciasPorDia(0)
                .diasOperacion(0)
                .busesNecesarios(0)
                .busesDisponibles(0)
                .tieneCapacidadSuficiente(false)
                .frecuencias(Collections.emptyList())
                .advertencias(Collections.emptyList())
                .errores(errores)
                .build();
    }

    private String normalizarDiaSemana(String dia) {
        // Convertir acentos
        return dia.replace("MIÉRCOLES", "MIERCOLES")
                  .replace("SÁBADO", "SABADO");
    }

    private double calcularDistanciaAproximada(Terminal origen, Terminal destino) {
        // Aproximación simple basada en diferencia de coordenadas
        // En producción usar API de mapas
        if (origen.getLatitud() != null && origen.getLongitud() != null &&
            destino.getLatitud() != null && destino.getLongitud() != null) {
            
            double dLat = Math.toRadians(destino.getLatitud() - origen.getLatitud());
            double dLon = Math.toRadians(destino.getLongitud() - origen.getLongitud());
            double lat1 = Math.toRadians(origen.getLatitud());
            double lat2 = Math.toRadians(destino.getLatitud());

            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                       Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return 6371 * c; // Radio de la tierra en km
        }
        
        // Distancia por defecto si no hay coordenadas
        return 100.0;
    }

    /**
     * Determina el tipo de frecuencia basándose en la distancia de la ruta.
     * 
     * Regla de negocio:
     * - Si la distancia es mayor al umbral (default 100km) -> INTERPROVINCIAL
     * - Si la distancia es menor o igual al umbral -> INTRAPROVINCIAL
     */
    private TipoFrecuencia determinarTipoFrecuencia(Ruta ruta, FrecuenciaConfigCooperativa config) {
        double umbralKm = 100.0; // Default
        
        if (config != null && config.getUmbralInterprovincialKm() != null) {
            umbralKm = config.getUmbralInterprovincialKm();
        }
        
        Double distanciaRuta = ruta.getDistanciaKm();
        if (distanciaRuta == null) {
            // Si no hay distancia configurada, intentar calcular desde terminales
            if (ruta.getTerminalOrigen() != null && ruta.getTerminalDestino() != null) {
                distanciaRuta = calcularDistanciaAproximada(ruta.getTerminalOrigen(), ruta.getTerminalDestino());
            } else {
                // Asumir interprovincial por seguridad
                return TipoFrecuencia.INTERPROVINCIAL;
            }
        }
        
        if (distanciaRuta > umbralKm) {
            log.debug("Ruta {} clasificada como INTERPROVINCIAL (distancia: {}km > umbral: {}km)", 
                ruta.getNombre(), distanciaRuta, umbralKm);
            return TipoFrecuencia.INTERPROVINCIAL;
        } else {
            log.debug("Ruta {} clasificada como INTRAPROVINCIAL (distancia: {}km <= umbral: {}km)", 
                ruta.getNombre(), distanciaRuta, umbralKm);
            return TipoFrecuencia.INTRAPROVINCIAL;
        }
    }
}
