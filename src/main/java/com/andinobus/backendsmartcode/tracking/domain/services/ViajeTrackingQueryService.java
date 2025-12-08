package com.andinobus.backendsmartcode.tracking.domain.services;

import com.andinobus.backendsmartcode.tracking.application.dto.ViajeActivoDTO;
import com.andinobus.backendsmartcode.operacion.domain.entities.Viaje;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeRepository;
import com.andinobus.backendsmartcode.ventas.domain.repositories.ReservaRepository;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Terminal;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.TerminalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para consultar información de viajes activos
 * Usado por los dashboards de tracking
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ViajeTrackingQueryService {

    private final ViajeRepository viajeRepository;
    private final ReservaRepository reservaRepository;
    private final TerminalRepository terminalRepository;

    /**
     * Obtiene todos los viajes activos del sistema
     * Para Super Admin
     */
    public List<ViajeActivoDTO> obtenerViajesActivos() {
        log.info("Obteniendo todos los viajes activos del sistema");

        LocalDate hoy = LocalDate.now();
        List<Viaje> viajes = new ArrayList<>();
        viajes.addAll(viajeRepository.findActivosByFecha(hoy.minusDays(1)));
        viajes.addAll(viajeRepository.findActivosByFecha(hoy));
        viajes.addAll(viajeRepository.findActivosByFecha(hoy.plusDays(1)));

        return viajes.stream()
                .filter(v -> "EN_CURSO".equals(v.getEstado()) ||
                             "PROGRAMADO".equals(v.getEstado()) ||
                             "EN_TERMINAL".equals(v.getEstado()))
                .map(this::convertirAViajeActivoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene viajes activos de una cooperativa específica
     * Para Admin de Cooperativa
     */
    public List<ViajeActivoDTO> obtenerViajesActivosPorCooperativa(Long cooperativaId) {
        log.info("Obteniendo viajes activos de la cooperativa: {}", cooperativaId);

        LocalDate hoy = LocalDate.now();
        List<Viaje> viajes = new ArrayList<>();
        viajes.addAll(viajeRepository.findActivosByFecha(hoy.minusDays(1)));
        viajes.addAll(viajeRepository.findActivosByFecha(hoy));
        viajes.addAll(viajeRepository.findActivosByFecha(hoy.plusDays(1)));

        return viajes.stream()
                .filter(v -> v.getFrecuencia() != null && v.getFrecuencia().getCooperativa() != null
                        && cooperativaId.equals(v.getFrecuencia().getCooperativa().getId()))
                .filter(v -> "EN_CURSO".equals(v.getEstado()) ||
                             "PROGRAMADO".equals(v.getEstado()) ||
                             "EN_TERMINAL".equals(v.getEstado()))
                .map(this::convertirAViajeActivoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene viajes activos para un cliente específico
     * Basado en los boletos que ha comprado
     */
    public List<ViajeActivoDTO> obtenerViajesActivosPorCliente(String emailCliente) {
        log.info("Obteniendo viajes activos del cliente: {}", emailCliente);

        // Obtener IDs de viajes con reservas del cliente en estado PAGADO
        List<Long> viajeIds = reservaRepository.findByClienteEmail(emailCliente).stream()
                .filter(r -> "PAGADO".equals(r.getEstado()))
                .map(r -> r.getViaje().getId())
                .distinct()
                .collect(Collectors.toList());

        if (viajeIds.isEmpty()) {
            return List.of();
        }

        // Obtener viajes de esos IDs
        List<Viaje> viajes = viajeRepository.findAllById(viajeIds);

        return viajes.stream()
                .filter(v -> "EN_CURSO".equals(v.getEstado()) ||
                             "PROGRAMADO".equals(v.getEstado()))
                .map(this::convertirAViajeActivoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el detalle completo de un viaje específico
     */
    public ViajeActivoDTO obtenerDetalleViaje(Long viajeId) {
        log.info("Obteniendo detalle del viaje: {}", viajeId);

        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado: " + viajeId));

        return convertirAViajeActivoDTO(viaje);
    }

    /**
     * Convierte una entidad Viaje a ViajeActivoDTO
     */
    private ViajeActivoDTO convertirAViajeActivoDTO(Viaje viaje) {
        var frecuencia = viaje.getFrecuencia();
        var bus = viaje.getBus();
        var cooperativa = (bus != null) ? bus.getCooperativa() : null;
        var chofer = viaje.getChofer();

        // Contar pasajeros (reservas pagadas) sumando asientos
        int numeroPasajeros = reservaRepository.findByViajeId(viaje.getId()).stream()
                .filter(r -> "PAGADO".equals(r.getEstado()))
                .mapToInt(r -> r.getAsientos() != null ? r.getAsientos() : 0)
                .sum();

        // Obtener coordenadas de terminales para la ruta
        BigDecimal terminalOrigenLat = null;
        BigDecimal terminalOrigenLon = null;
        BigDecimal terminalDestinoLat = null;
        BigDecimal terminalDestinoLon = null;
        String terminalOrigenNombre = null;
        String terminalDestinoNombre = null;

        if (frecuencia != null) {
            // Obtener nombres de origen y destino de la frecuencia
            // Formato: "PROVINCIA|Canton|numero" -> extraemos el cantón (parte del medio)
            String origenCompleto = frecuencia.getOrigen();
            String destinoCompleto = frecuencia.getDestino();
            
            terminalOrigenNombre = extraerCantonDeRuta(origenCompleto);
            terminalDestinoNombre = extraerCantonDeRuta(destinoCompleto);
            
            log.debug("Extrayendo cantones - Origen: '{}' -> '{}', Destino: '{}' -> '{}'", 
                origenCompleto, terminalOrigenNombre, destinoCompleto, terminalDestinoNombre);
            
            // Buscar terminal de origen por nombre (cantón) para obtener coordenadas
            if (terminalOrigenNombre != null) {
                // Primero buscar por cantón (retorna lista, tomamos el primero)
                List<Terminal> terminalesPorCanton = terminalRepository.findByCantonIgnoreCase(terminalOrigenNombre);
                Optional<Terminal> terminalOrigen = terminalesPorCanton.isEmpty() 
                    ? Optional.empty() 
                    : Optional.of(terminalesPorCanton.get(0));
                
                if (terminalOrigen.isEmpty()) {
                    // Intentar buscar por nombre del terminal si no se encuentra por cantón
                    terminalOrigen = terminalRepository.findByNombreIgnoreCase(terminalOrigenNombre);
                }
                if (terminalOrigen.isPresent()) {
                    Terminal term = terminalOrigen.get();
                    if (term.getLatitud() != null && term.getLongitud() != null) {
                        terminalOrigenLat = BigDecimal.valueOf(term.getLatitud());
                        terminalOrigenLon = BigDecimal.valueOf(term.getLongitud());
                        terminalOrigenNombre = term.getNombre(); // Usar nombre real del terminal
                        log.debug("Terminal origen encontrado: {} -> lat={}, lon={}", 
                            terminalOrigenNombre, terminalOrigenLat, terminalOrigenLon);
                    }
                }
            }
            
            // Buscar terminal de destino por nombre (cantón) para obtener coordenadas
            if (terminalDestinoNombre != null) {
                // Primero buscar por cantón (retorna lista, tomamos el primero)
                List<Terminal> terminalesPorCanton = terminalRepository.findByCantonIgnoreCase(terminalDestinoNombre);
                Optional<Terminal> terminalDestino = terminalesPorCanton.isEmpty() 
                    ? Optional.empty() 
                    : Optional.of(terminalesPorCanton.get(0));
                
                if (terminalDestino.isEmpty()) {
                    // Intentar buscar por nombre del terminal si no se encuentra por cantón
                    terminalDestino = terminalRepository.findByNombreIgnoreCase(terminalDestinoNombre);
                }
                if (terminalDestino.isPresent()) {
                    Terminal term = terminalDestino.get();
                    if (term.getLatitud() != null && term.getLongitud() != null) {
                        terminalDestinoLat = BigDecimal.valueOf(term.getLatitud());
                        terminalDestinoLon = BigDecimal.valueOf(term.getLongitud());
                        terminalDestinoNombre = term.getNombre(); // Usar nombre real del terminal
                        log.debug("Terminal destino encontrado: {} -> lat={}, lon={}", 
                            terminalDestinoNombre, terminalDestinoLat, terminalDestinoLon);
                    }
                }
            }
        }

        return ViajeActivoDTO.builder()
                .id(viaje.getId())
                .viajeId(viaje.getId())
                // Bus
                .busPlaca(bus != null ? bus.getPlaca() : null)
                .busId(bus != null ? bus.getId() : null)
                // Cooperativa
                .cooperativaNombre(cooperativa != null ? cooperativa.getNombre() : null)
                .cooperativaId(cooperativa != null ? cooperativa.getId() : null)
                // Ruta / Frecuencia
                .rutaOrigen(frecuencia != null ? frecuencia.getOrigen() : null)
                .rutaDestino(frecuencia != null ? frecuencia.getDestino() : null)
                .rutaNombre(frecuencia != null ? (frecuencia.getOrigen() + " - " + frecuencia.getDestino()) : null)
                // Chofer
                .choferNombre(chofer != null ? chofer.getNombres() : "Sin asignar")
                .choferApellido(chofer != null ? chofer.getApellidos() : "")
                .choferId(chofer != null ? chofer.getId() : null)
                // Viaje
                .fechaSalida(viaje.getFecha() != null ? viaje.getFecha().format(DateTimeFormatter.ISO_LOCAL_DATE) : null)
                .horaSalida(viaje.getHoraSalida() != null ? viaje.getHoraSalida().toString() : null)
                .horaLlegadaEstimada(viaje.getHoraLlegadaEstimada() != null ? viaje.getHoraLlegadaEstimada().toString() : null)
                .estado(viaje.getEstado())
                // Capacidad
                .numeroPasajeros(numeroPasajeros)
                .capacidadTotal(bus != null ? bus.getCapacidadAsientos() : null)
                // Posición GPS (convertir Double -> BigDecimal)
                .latitudActual(viaje.getLatitudActual() != null ? BigDecimal.valueOf(viaje.getLatitudActual()) : null)
                .longitudActual(viaje.getLongitudActual() != null ? BigDecimal.valueOf(viaje.getLongitudActual()) : null)
                .velocidadKmh(null) // Se podría obtener de la última PosicionViaje
                .ultimaActualizacion(viaje.getUltimaActualizacion())
                // Tiempos reales
                .horaInicioReal(viaje.getHoraInicioReal())
                .horaFinReal(viaje.getHoraFinReal())
                // Coordenadas de terminales para mostrar ruta en mapa
                .terminalOrigenLatitud(terminalOrigenLat)
                .terminalOrigenLongitud(terminalOrigenLon)
                .terminalDestinoLatitud(terminalDestinoLat)
                .terminalDestinoLongitud(terminalDestinoLon)
                .terminalOrigenNombre(terminalOrigenNombre)
                .terminalDestinoNombre(terminalDestinoNombre)
                .build();
    }
    
    /**
     * Extrae el cantón de una ruta con formato "PROVINCIA|Canton|numero"
     * Si el formato no coincide, retorna el string original
     * 
     * @param rutaCompleta La ruta en formato "PROVINCIA|Canton|numero"
     * @return El cantón extraído o el string original si no tiene el formato esperado
     */
    private String extraerCantonDeRuta(String rutaCompleta) {
        if (rutaCompleta == null || rutaCompleta.isEmpty()) {
            return null;
        }
        
        // Verificar si tiene el formato con pipes
        if (rutaCompleta.contains("|")) {
            String[] partes = rutaCompleta.split("\\|");
            if (partes.length >= 2) {
                // El cantón está en la segunda posición (índice 1)
                return partes[1].trim();
            }
        }
        
        // Si no tiene el formato esperado, retornar el string original
        return rutaCompleta.trim();
    }
}
