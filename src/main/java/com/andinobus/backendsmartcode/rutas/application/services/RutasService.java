package com.andinobus.backendsmartcode.rutas.application.services;

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
    private final ViajeRepository viajeRepository;
    private final BusAsientoConfigRepository busAsientoConfigRepository;

    /**
     * Busca rutas disponibles según criterios
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

        // 1. Buscar frecuencias según filtros
        List<Frecuencia> frecuencias;
        if ((origen == null || origen.isEmpty()) && (destino == null || destino.isEmpty())) {
            // Sin filtros de origen/destino: obtener todas las frecuencias activas
            frecuencias = frecuenciaRepository.findByActivaTrue();
        } else if (origen != null && !origen.isEmpty() && destino != null && !destino.isEmpty()) {
            // Con origen y destino
            frecuencias = frecuenciaRepository.findByOrigenAndDestinoAndActivaTrue(origen, destino);
        } else if (origen != null && !origen.isEmpty()) {
            // Solo origen
            frecuencias = frecuenciaRepository.findByOrigenAndActivaTrue(origen);
        } else {
            // Solo destino
            frecuencias = frecuenciaRepository.findByDestinoAndActivaTrue(destino);
        }

        // 2. Filtrar por cooperativa si se especifica
        if (cooperativa != null && !cooperativa.isEmpty()) {
            frecuencias = frecuencias.stream()
                    .filter(f -> f.getCooperativa().getNombre().toLowerCase().contains(cooperativa.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // 3. Crear o buscar viajes para cada frecuencia en la fecha especificada
        List<RutasDtos.SearchRouteItem> items = new ArrayList<>();
        for (Frecuencia frecuencia : frecuencias) {
            // Buscar si ya existe un viaje para esta frecuencia en esta fecha
            List<Viaje> viajes = viajeRepository.findByFrecuenciaIdAndFecha(frecuencia.getId(), fecha);
            
            Viaje viaje;
            if (viajes.isEmpty()) {
                // Si no existe, no lo creamos automáticamente, solo mostramos la frecuencia disponible
                viaje = null;
            } else {
                viaje = viajes.get(0); // Tomamos el primer viaje si existe
            }

            // Obtener configuración de asientos del bus si hay un bus asignado
            Map<String, Integer> asientosPorTipo = new HashMap<>();
            if (viaje != null && viaje.getBus() != null) {
                List<BusAsientoConfig> configs = busAsientoConfigRepository.findByBusId(viaje.getBus().getId());
                for (BusAsientoConfig config : configs) {
                    asientosPorTipo.put(config.getTipoAsiento(), config.getCantidad());
                }
            } else {
                // Si no hay bus asignado, usar valores por defecto
                asientosPorTipo.put("Normal", 32);
                asientosPorTipo.put("VIP", 8);
            }

            // Filtrar por tipo de asiento si se especifica
            if (tipoAsiento != null && !tipoAsiento.isEmpty()) {
                if (!asientosPorTipo.containsKey(tipoAsiento)) {
                    continue; // Esta frecuencia no tiene el tipo de asiento buscado
                }
            }

            // Calcular duración estimada en formato HH:MM
            String duracionEstimada = frecuencia.getDuracionEstimadaMin() != null
                    ? String.format("%02d:%02d", 
                            frecuencia.getDuracionEstimadaMin() / 60, 
                            frecuencia.getDuracionEstimadaMin() % 60)
                    : "00:00";

            // Determinar fecha para mostrar (usar la fecha del viaje si existe, sino usar la fecha buscada o hoy)
            String fechaMostrar = viaje != null && viaje.getFecha() != null 
                    ? viaje.getFecha().toString() 
                    : (fecha != null ? fecha.toString() : LocalDate.now().toString());

            RutasDtos.SearchRouteItem item = RutasDtos.SearchRouteItem.builder()
                    .frecuenciaId(frecuencia.getId())
                    .cooperativaId(frecuencia.getCooperativa().getId())
                    .cooperativa(frecuencia.getCooperativa().getNombre())
                    .origen(frecuencia.getOrigen())
                    .destino(frecuencia.getDestino())
                    .horaSalida(frecuencia.getHoraSalida().toString())
                    .duracionEstimada(duracionEstimada)
                    .tipoViaje(tipoViaje != null ? tipoViaje : "directo")
                    .asientosPorTipo(asientosPorTipo)
                    .fecha(fechaMostrar)
                    .build();

            items.add(item);
        }

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
