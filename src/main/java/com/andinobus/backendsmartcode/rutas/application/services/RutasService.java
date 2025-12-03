package com.andinobus.backendsmartcode.rutas.application.services;

import com.andinobus.backendsmartcode.admin.domain.entities.FrecuenciaViaje;
import com.andinobus.backendsmartcode.admin.domain.repositories.FrecuenciaViajeRepository;
import com.andinobus.backendsmartcode.catalogos.domain.entities.BusAsientoConfig;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Frecuencia;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.BusAsientoConfigRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.FrecuenciaRepository;
import com.andinobus.backendsmartcode.operacion.domain.entities.Viaje;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeRepository;
import com.andinobus.backendsmartcode.rutas.api.dto.RutasDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Profile("dev")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RutasService {

    private final FrecuenciaRepository frecuenciaRepository;
    private final FrecuenciaViajeRepository frecuenciaViajeRepository;
    private final ViajeRepository viajeRepository;
    private final BusAsientoConfigRepository busAsientoConfigRepository;

    /**
     * Busca rutas disponibles según criterios - ahora busca en frecuencia_viaje
     */
    public RutasDtos.SearchRouteResponse buscarRutas(
            String origen,
            String destino,
            String fechaStr,
            String cooperativa,
            String tipoAsiento,
            String tipoViaje,
            Integer page,
            Integer size
    ) {
        LocalDate fecha = fechaStr != null && !fechaStr.isEmpty() ? LocalDate.parse(fechaStr) : null;

        // 1. Obtener todas las frecuencias de viaje activas
        List<FrecuenciaViaje> frecuenciasViaje = frecuenciaViajeRepository.findAll().stream()
                .filter(f -> f.getActivo() != null && f.getActivo())
                .collect(Collectors.toList());

        // 2. Filtrar por origen si se especifica
        if (origen != null && !origen.isEmpty()) {
            frecuenciasViaje = frecuenciasViaje.stream()
                    .filter(f -> {
                        // Buscar en terminal origen
                        if (f.getTerminalOrigen() != null) {
                            String terminalNombre = f.getTerminalOrigen().getNombre();
                            String terminalCanton = f.getTerminalOrigen().getCanton();
                            if ((terminalNombre != null && terminalNombre.toLowerCase().contains(origen.toLowerCase())) ||
                                (terminalCanton != null && terminalCanton.toLowerCase().contains(origen.toLowerCase()))) {
                                return true;
                            }
                        }
                        // Buscar en ruta
                        if (f.getRuta() != null && f.getRuta().getOrigen() != null) {
                            return f.getRuta().getOrigen().toLowerCase().contains(origen.toLowerCase());
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }

        // 3. Filtrar por destino si se especifica
        if (destino != null && !destino.isEmpty()) {
            frecuenciasViaje = frecuenciasViaje.stream()
                    .filter(f -> {
                        // Buscar en terminal destino
                        if (f.getTerminalDestino() != null) {
                            String terminalNombre = f.getTerminalDestino().getNombre();
                            String terminalCanton = f.getTerminalDestino().getCanton();
                            if ((terminalNombre != null && terminalNombre.toLowerCase().contains(destino.toLowerCase())) ||
                                (terminalCanton != null && terminalCanton.toLowerCase().contains(destino.toLowerCase()))) {
                                return true;
                            }
                        }
                        // Buscar en ruta
                        if (f.getRuta() != null && f.getRuta().getDestino() != null) {
                            return f.getRuta().getDestino().toLowerCase().contains(destino.toLowerCase());
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }

        // 4. Filtrar por cooperativa si se especifica
        if (cooperativa != null && !cooperativa.isEmpty()) {
            frecuenciasViaje = frecuenciasViaje.stream()
                    .filter(f -> {
                        if (f.getCooperativa() != null && f.getCooperativa().getNombre() != null) {
                            return f.getCooperativa().getNombre().toLowerCase().contains(cooperativa.toLowerCase());
                        }
                        if (f.getBus() != null && f.getBus().getCooperativa() != null) {
                            return f.getBus().getCooperativa().getNombre().toLowerCase().contains(cooperativa.toLowerCase());
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }

        // 5. Crear items de respuesta
        List<RutasDtos.SearchRouteItem> items = new ArrayList<>();
        for (FrecuenciaViaje fv : frecuenciasViaje) {
            // Obtener origen y destino
            String origenNombre = obtenerOrigenFrecuenciaViaje(fv);
            String destinoNombre = obtenerDestinoFrecuenciaViaje(fv);
            String cooperativaNombre = obtenerCooperativaNombre(fv);
            Long cooperativaId = obtenerCooperativaId(fv);

            // Obtener configuración de asientos del bus
            Map<String, Integer> asientosPorTipo = new HashMap<>();
            if (fv.getBus() != null) {
                List<BusAsientoConfig> configs = busAsientoConfigRepository.findByBusId(fv.getBus().getId());
                for (BusAsientoConfig config : configs) {
                    asientosPorTipo.put(config.getTipoAsiento(), config.getCantidad());
                }
            }
            if (asientosPorTipo.isEmpty()) {
                // Valores por defecto si no hay configuración
                asientosPorTipo.put("Normal", fv.getAsientosDisponibles() != null ? fv.getAsientosDisponibles() : 40);
            }

            // Calcular duración estimada en formato HH:MM
            String duracionEstimada = "00:00";
            if (fv.getDuracionEstimadaMinutos() != null) {
                duracionEstimada = String.format("%02d:%02d", 
                        fv.getDuracionEstimadaMinutos() / 60, 
                        fv.getDuracionEstimadaMinutos() % 60);
            }

            // Determinar fecha
            String fechaMostrar = fecha != null ? fecha.toString() : LocalDate.now().toString();

            // Determinar tipo de viaje
            String tipoViajeStr = fv.getTipoFrecuencia() != null 
                    ? fv.getTipoFrecuencia().name() 
                    : "INTERPROVINCIAL";

            RutasDtos.SearchRouteItem item = RutasDtos.SearchRouteItem.builder()
                    .frecuenciaId(fv.getId())
                    .cooperativaId(cooperativaId)
                    .cooperativa(cooperativaNombre)
                    .origen(origenNombre)
                    .destino(destinoNombre)
                    .horaSalida(fv.getHoraSalida() != null ? fv.getHoraSalida().toString() : "00:00")
                    .duracionEstimada(duracionEstimada)
                    .tipoViaje(tipoViajeStr)
                    .asientosPorTipo(asientosPorTipo)
                    .fecha(fechaMostrar)
                    .precio(fv.getPrecioBase())
                    .build();

            items.add(item);
        }

        // Ordenar por hora de salida
        items.sort(Comparator.comparing(RutasDtos.SearchRouteItem::getHoraSalida));

        // Aplicar paginación
        int start = page * size;
        int end = Math.min(start + size, items.size());
        List<RutasDtos.SearchRouteItem> paginatedItems = start < items.size() 
                ? items.subList(start, end) 
                : Collections.<RutasDtos.SearchRouteItem>emptyList();

        return RutasDtos.SearchRouteResponse.builder()
                .items(paginatedItems)
                .total(items.size())
                .page(page)
                .size(size)
                .build();
    }

    private String obtenerOrigenFrecuenciaViaje(FrecuenciaViaje fv) {
        if (fv.getTerminalOrigen() != null) {
            return fv.getTerminalOrigen().getCanton() != null 
                    ? fv.getTerminalOrigen().getCanton() 
                    : fv.getTerminalOrigen().getNombre();
        }
        if (fv.getRuta() != null && fv.getRuta().getOrigen() != null) {
            return fv.getRuta().getOrigen();
        }
        return "Sin origen";
    }

    private String obtenerDestinoFrecuenciaViaje(FrecuenciaViaje fv) {
        if (fv.getTerminalDestino() != null) {
            return fv.getTerminalDestino().getCanton() != null 
                    ? fv.getTerminalDestino().getCanton() 
                    : fv.getTerminalDestino().getNombre();
        }
        if (fv.getRuta() != null && fv.getRuta().getDestino() != null) {
            return fv.getRuta().getDestino();
        }
        return "Sin destino";
    }

    private String obtenerCooperativaNombre(FrecuenciaViaje fv) {
        if (fv.getCooperativa() != null && fv.getCooperativa().getNombre() != null) {
            return fv.getCooperativa().getNombre();
        }
        if (fv.getBus() != null && fv.getBus().getCooperativa() != null) {
            return fv.getBus().getCooperativa().getNombre();
        }
        return "Cooperativa";
    }

    private Long obtenerCooperativaId(FrecuenciaViaje fv) {
        if (fv.getCooperativa() != null) {
            return fv.getCooperativa().getId();
        }
        if (fv.getBus() != null && fv.getBus().getCooperativa() != null) {
            return fv.getBus().getCooperativa().getId();
        }
        return 0L;
    }

    /**
     * Obtiene viajes programados para una fecha
     */
    public RutasDtos.ViajesResponse viajesPorFecha(String fechaStr, Integer page, Integer size) {
        LocalDate fecha = LocalDate.parse(fechaStr);
        List<Viaje> viajes = viajeRepository.findActivosByFecha(fecha);

        List<RutasDtos.ViajeItem> items = viajes.stream().map(v -> RutasDtos.ViajeItem.builder()
                .id(v.getId())
                .frecuenciaId(v.getFrecuencia().getId())
                .fecha(v.getFecha().toString())
                .origen(v.getFrecuencia().getOrigen())
                .destino(v.getFrecuencia().getDestino())
                .horaSalida(v.getHoraSalidaProgramada().toString())
                .estado(v.getEstado())
                .build()
        ).collect(Collectors.toList());

        // Aplicar paginación
        int start = page * size;
        int end = Math.min(start + size, items.size());
        List<RutasDtos.ViajeItem> paginatedItems = start < items.size() 
                ? items.subList(start, end) 
                : Collections.<RutasDtos.ViajeItem>emptyList();

        return RutasDtos.ViajesResponse.builder()
                .items(paginatedItems)
                .total(items.size())
                .page(page)
                .size(size)
                .build();
    }

    /**
     * Obtiene disponibilidad de asientos para un viaje
     */
    public RutasDtos.DisponibilidadResponse disponibilidad(Long viajeId) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));

        if (viaje.getBus() == null) {
            throw new RuntimeException("No hay bus asignado a este viaje");
        }

        List<BusAsientoConfig> configs = busAsientoConfigRepository.findByBusId(viaje.getBus().getId());
        
        int totalAsientos = configs.stream().mapToInt(BusAsientoConfig::getCantidad).sum();
        Map<String, Integer> porTipo = configs.stream()
                .collect(Collectors.toMap(BusAsientoConfig::getTipoAsiento, BusAsientoConfig::getCantidad));

        // TODO: Calcular asientos ocupados desde viaje_asiento cuando se implementen reservas
        int disponibles = totalAsientos;

        return RutasDtos.DisponibilidadResponse.builder()
                .viajeId(viajeId)
                .totalAsientos(totalAsientos)
                .disponibles(disponibles)
                .porTipo(porTipo)
                .build();
    }

    /**
     * Obtiene información del bus asignado a un viaje
     */
    public RutasDtos.BusFichaResponse busDeViaje(Long viajeId) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));

        if (viaje.getBus() == null) {
            throw new RuntimeException("No hay bus asignado a este viaje");
        }

        return RutasDtos.BusFichaResponse.builder()
                .viajeId(viajeId)
                .busId(viaje.getBus().getId())
                .cooperativa(viaje.getFrecuencia().getCooperativa().getNombre())
                .numeroInterno(viaje.getBus().getNumeroInterno())
                .placa(viaje.getBus().getPlaca())
                .chasisMarca(viaje.getBus().getChasisMarca())
                .carroceriaMarca(viaje.getBus().getCarroceriaMarca())
                .fotoUrl(viaje.getBus().getFotoUrl())
                .build();
    }
}
