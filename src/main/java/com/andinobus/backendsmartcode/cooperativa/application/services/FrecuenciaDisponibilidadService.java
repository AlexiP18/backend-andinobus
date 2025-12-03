package com.andinobus.backendsmartcode.cooperativa.application.services;

import com.andinobus.backendsmartcode.catalogos.domain.entities.*;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.CooperativaTerminalRepository;
import com.andinobus.backendsmartcode.catalogos.domain.repositories.TerminalRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.*;
import com.andinobus.backendsmartcode.cooperativa.api.dto.FrecuenciaConfigDtos.*;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.*;
import com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FrecuenciaDisponibilidadService {

    private final FrecuenciaConfigCooperativaRepository configRepository;
    private final CooperativaTerminalRepository cooperativaTerminalRepository;
    private final BusRepository busRepository;
    private final BusChoferRepository busChoferRepository;
    private final BusOperacionDiariaRepository busOperacionDiariaRepository;
    private final ChoferHorasTrabajadasRepository choferHorasRepository;
    private final TerminalRepository terminalRepository;

    /**
     * Obtiene la configuración de frecuencias de una cooperativa
     * Si no existe, retorna una configuración con valores por defecto
     */
    @Transactional(readOnly = true)
    public FrecuenciaConfigResponse getConfiguracion(Long cooperativaId) {
        var configOpt = configRepository.findByCooperativaId(cooperativaId);
        
        if (configOpt.isEmpty()) {
            // Retornar configuración por defecto
            return FrecuenciaConfigResponse.builder()
                    .id(0L)
                    .cooperativaId(cooperativaId)
                    .cooperativaNombre("Cooperativa")
                    .precioBasePorKm(0.02)
                    .factorDieselPorKm(0.12)
                    .precioDiesel(1.80)
                    .margenGananciaPorcentaje(30.0)
                    .maxHorasDiariasChofer(8)
                    .maxHorasExcepcionales(10)
                    .maxDiasExcepcionalesSemana(2)
                    .tiempoDescansoEntreViajesMinutos(30)
                    .tiempoMinimoParadaBusMinutos(15)
                    .horasOperacionMaxBus(24)
                    .intervaloMinimoFrecuenciasMinutos(30)
                    .horaInicioOperacion("05:00")
                    .horaFinOperacion("23:00")
                    .build();
        }
        
        var config = configOpt.get();
        return FrecuenciaConfigResponse.builder()
                .id(config.getId())
                .cooperativaId(cooperativaId)
                .cooperativaNombre(config.getCooperativa() != null ? config.getCooperativa().getNombre() : "Cooperativa")
                .precioBasePorKm(config.getPrecioBasePorKm())
                .factorDieselPorKm(config.getFactorDieselPorKm())
                .precioDiesel(config.getPrecioDiesel())
                .margenGananciaPorcentaje(config.getMargenGananciaPorcentaje())
                .maxHorasDiariasChofer(config.getMaxHorasDiariasChofer())
                .maxHorasExcepcionales(config.getMaxHorasExcepcionales())
                .maxDiasExcepcionalesSemana(config.getMaxDiasExcepcionalesSemana())
                .tiempoDescansoEntreViajesMinutos(config.getTiempoDescansoEntreViajesMinutos())
                .tiempoMinimoParadaBusMinutos(config.getTiempoMinimoParadaBusMinutos())
                .horasOperacionMaxBus(config.getHorasOperacionMaxBus())
                .intervaloMinimoFrecuenciasMinutos(config.getIntervaloMinimoFrecuenciasMinutos())
                .horaInicioOperacion(config.getHoraInicioOperacion().toString())
                .horaFinOperacion(config.getHoraFinOperacion().toString())
                .build();
    }

    /**
     * Actualiza la configuración de frecuencias de una cooperativa
     */
    @Transactional
    public FrecuenciaConfigResponse updateConfiguracion(Long cooperativaId, UpdateFrecuenciaConfigRequest request) {
        var config = configRepository.findByCooperativaId(cooperativaId)
                .orElseThrow(() -> new EntityNotFoundException("Configuración no encontrada para la cooperativa"));

        if (request.getPrecioBasePorKm() != null) config.setPrecioBasePorKm(request.getPrecioBasePorKm());
        if (request.getFactorDieselPorKm() != null) config.setFactorDieselPorKm(request.getFactorDieselPorKm());
        if (request.getPrecioDiesel() != null) config.setPrecioDiesel(request.getPrecioDiesel());
        if (request.getMargenGananciaPorcentaje() != null) config.setMargenGananciaPorcentaje(request.getMargenGananciaPorcentaje());
        if (request.getMaxHorasDiariasChofer() != null) config.setMaxHorasDiariasChofer(request.getMaxHorasDiariasChofer());
        if (request.getMaxHorasExcepcionales() != null) config.setMaxHorasExcepcionales(request.getMaxHorasExcepcionales());
        if (request.getMaxDiasExcepcionalesSemana() != null) config.setMaxDiasExcepcionalesSemana(request.getMaxDiasExcepcionalesSemana());
        if (request.getTiempoDescansoEntreViajesMinutos() != null) config.setTiempoDescansoEntreViajesMinutos(request.getTiempoDescansoEntreViajesMinutos());
        if (request.getTiempoMinimoParadaBusMinutos() != null) config.setTiempoMinimoParadaBusMinutos(request.getTiempoMinimoParadaBusMinutos());
        if (request.getHorasOperacionMaxBus() != null) config.setHorasOperacionMaxBus(request.getHorasOperacionMaxBus());
        if (request.getIntervaloMinimoFrecuenciasMinutos() != null) config.setIntervaloMinimoFrecuenciasMinutos(request.getIntervaloMinimoFrecuenciasMinutos());
        if (request.getHoraInicioOperacion() != null) config.setHoraInicioOperacion(LocalTime.parse(request.getHoraInicioOperacion()));
        if (request.getHoraFinOperacion() != null) config.setHoraFinOperacion(LocalTime.parse(request.getHoraFinOperacion()));

        configRepository.save(config);
        return getConfiguracion(cooperativaId);
    }

    /**
     * Obtiene las rutas disponibles para una cooperativa (basado en sus terminales)
     */
    @Transactional(readOnly = true)
    public List<RutaDisponibleResponse> getRutasDisponibles(Long cooperativaId) {
        // Obtener terminales de la cooperativa
        List<CooperativaTerminal> terminalesCooperativa = cooperativaTerminalRepository
                .findByCooperativaIdWithTerminal(cooperativaId);
        
        if (terminalesCooperativa.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> terminalIds = terminalesCooperativa.stream()
                .map(ct -> ct.getTerminal().getId())
                .collect(Collectors.toSet());

        // Obtener configuración para cálculo de precio
        var config = configRepository.findByCooperativaId(cooperativaId).orElse(null);

        // Generar rutas entre todos los pares de terminales
        List<RutaDisponibleResponse> rutas = new ArrayList<>();
        List<Terminal> terminales = terminalesCooperativa.stream()
                .map(CooperativaTerminal::getTerminal)
                .collect(Collectors.toList());

        for (Terminal origen : terminales) {
            for (Terminal destino : terminales) {
                if (!origen.getId().equals(destino.getId())) {
                    // Calcular distancia aproximada (en producción usar API de mapas)
                    double distanciaKm = calcularDistanciaAproximada(origen, destino);
                    int duracionMinutos = (int) (distanciaKm * 1.2); // Aproximación: 50 km/h promedio

                    // Calcular precio sugerido
                    double precioSugerido = config != null 
                            ? config.calcularPrecioSugerido(distanciaKm)
                            : distanciaKm * 0.05; // Default: $0.05 por km

                    // Encontrar terminales intermedios
                    List<TerminalIntermedio> intermedios = encontrarTerminalesIntermedios(
                            origen, destino, terminales);

                    rutas.add(RutaDisponibleResponse.builder()
                            .rutaId(null) // Se generará al crear la frecuencia
                            .rutaNombre(origen.getNombre() + " - " + destino.getNombre())
                            .terminalOrigenId(origen.getId())
                            .terminalOrigenNombre(origen.getNombre())
                            .terminalOrigenCanton(origen.getCanton())
                            .terminalOrigenProvincia(origen.getProvincia())
                            .terminalDestinoId(destino.getId())
                            .terminalDestinoNombre(destino.getNombre())
                            .terminalDestinoCanton(destino.getCanton())
                            .terminalDestinoProvincia(destino.getProvincia())
                            .distanciaKm(distanciaKm)
                            .duracionEstimadaMinutos(duracionMinutos)
                            .precioSugerido(Math.round(precioSugerido * 100.0) / 100.0)
                            .terminalesIntermedios(intermedios)
                            .build());
                }
            }
        }

        return rutas;
    }

    /**
     * Obtiene disponibilidad de buses para una fecha
     */
    @Transactional(readOnly = true)
    public List<BusDisponibilidadResponse> getBusesDisponibles(Long cooperativaId, LocalDate fecha) {
        var config = configRepository.findByCooperativaId(cooperativaId).orElse(null);
        int maxHorasBus = config != null ? config.getHorasOperacionMaxBus() : 24;

        List<Bus> buses = busRepository.findByCooperativaIdAndActivoTrue(cooperativaId);
        List<BusDisponibilidadResponse> disponibilidad = new ArrayList<>();

        for (Bus bus : buses) {
            // Obtener operación del día
            var operacion = busOperacionDiariaRepository.findByBusIdAndFecha(bus.getId(), fecha)
                    .orElse(BusOperacionDiaria.builder().horasOperacion(0.0).frecuenciasRealizadas(0).build());

            double horasDisponibles = maxHorasBus - operacion.getHorasOperacion();
            boolean disponible = horasDisponibles > 0 && "DISPONIBLE".equals(bus.getEstado());
            String motivoNoDisponible = null;

            if (!bus.getActivo()) {
                disponible = false;
                motivoNoDisponible = "Bus inactivo";
            } else if (!"DISPONIBLE".equals(bus.getEstado())) {
                disponible = false;
                motivoNoDisponible = "Bus en estado: " + bus.getEstado();
            } else if (horasDisponibles <= 0) {
                disponible = false;
                motivoNoDisponible = "Sin horas disponibles para el día";
            }

            // Obtener choferes asignados
            List<BusChofer> choferesDelBus = busChoferRepository.findByBusIdAndActivoTrueOrderByOrdenAsc(bus.getId());
            List<ChoferAsignado> choferesAsignados = choferesDelBus.stream()
                    .map(bc -> {
                        var horasChofer = choferHorasRepository.findByChoferIdAndFecha(bc.getChofer().getId(), fecha)
                                .orElse(ChoferHorasTrabajadas.builder().horasTrabajadas(0.0).build());
                        
                        int maxHorasChofer = config != null ? config.getMaxHorasDiariasChofer() : 8;
                        double horasDisponiblesChofer = maxHorasChofer - horasChofer.getHorasTrabajadas();

                        return ChoferAsignado.builder()
                                .choferId(bc.getChofer().getId())
                                .nombre(bc.getChofer().getNombres() + " " + bc.getChofer().getApellidos())
                                .tipo(bc.getTipo())
                                .horasTrabajadasHoy(horasChofer.getHorasTrabajadas())
                                .horasDisponiblesHoy(horasDisponiblesChofer)
                                .disponible(horasDisponiblesChofer > 0)
                                .build();
                    })
                    .collect(Collectors.toList());

            disponibilidad.add(BusDisponibilidadResponse.builder()
                    .busId(bus.getId())
                    .placa(bus.getPlaca())
                    .numeroInterno(bus.getNumeroInterno())
                    .capacidadAsientos(bus.getCapacidadAsientos())
                    .estado(bus.getEstado())
                    .horasOperadasHoy(operacion.getHorasOperacion())
                    .horasDisponiblesHoy(horasDisponibles)
                    .frecuenciasHoy(operacion.getFrecuenciasRealizadas())
                    .disponible(disponible)
                    .motivoNoDisponible(motivoNoDisponible)
                    .choferesAsignados(choferesAsignados)
                    .build());
        }

        return disponibilidad;
    }

    /**
     * Obtiene disponibilidad de choferes para una fecha
     */
    @Transactional(readOnly = true)
    public List<ChoferDisponibilidadResponse> getChoferesDisponibles(Long cooperativaId, LocalDate fecha) {
        try {
            var config = configRepository.findByCooperativaId(cooperativaId).orElse(null);
            int maxHorasNormal = config != null ? config.getMaxHorasDiariasChofer() : 8;
            int maxHorasExcepcional = config != null ? config.getMaxHorasExcepcionales() : 10;
            int maxDiasExcepcionales = config != null ? config.getMaxDiasExcepcionalesSemana() : 2;

            // Calcular inicio de semana
            LocalDate inicioSemana = fecha.with(DayOfWeek.MONDAY);
            LocalDate finSemana = inicioSemana.plusDays(6);

            // Obtener choferes asignados a buses de la cooperativa
            List<BusChofer> asignaciones = busChoferRepository.findByCooperativaId(cooperativaId);
            if (asignaciones.isEmpty()) {
                return Collections.emptyList();
            }
            
            List<UsuarioCooperativa> choferes = asignaciones.stream()
                    .map(BusChofer::getChofer)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            List<ChoferDisponibilidadResponse> disponibilidad = new ArrayList<>();

            for (UsuarioCooperativa chofer : choferes) {
                double horasTrabajadas = 0.0;
                int frecuenciasRealizadas = 0;
                long diasExcepcionalesUsados = 0;
                
                try {
                    var horasHoy = choferHorasRepository.findByChoferIdAndFecha(chofer.getId(), fecha).orElse(null);
                    if (horasHoy != null) {
                        horasTrabajadas = horasHoy.getHorasTrabajadas() != null ? horasHoy.getHorasTrabajadas() : 0.0;
                        frecuenciasRealizadas = horasHoy.getFrecuenciasRealizadas() != null ? horasHoy.getFrecuenciasRealizadas() : 0;
                    }
                    diasExcepcionalesUsados = choferHorasRepository.countDiasExcepcionalesBySemana(
                            chofer.getId(), inicioSemana, finSemana);
                } catch (Exception e) {
                    // Si hay error al consultar horas (ej: tabla no existe), usar valores por defecto
                    log.warn("Error consultando horas de chofer {}: {}", chofer.getId(), e.getMessage());
                }

                double horasDisponibles = maxHorasNormal - horasTrabajadas;
                boolean puedeExcepcional = diasExcepcionalesUsados < maxDiasExcepcionales;
                boolean disponible = horasDisponibles > 0 || (puedeExcepcional && horasTrabajadas < maxHorasExcepcional);

                String motivoNoDisponible = null;
                if (!disponible) {
                    if (horasTrabajadas >= maxHorasExcepcional) {
                        motivoNoDisponible = "Alcanzó el máximo de horas excepcionales";
                    } else if (!puedeExcepcional) {
                        motivoNoDisponible = "Ya usó los días excepcionales de la semana";
                    } else {
                        motivoNoDisponible = "Sin horas disponibles";
                    }
                }

                // Buscar bus asignado
                var asignacionBus = busChoferRepository.findByChoferIdAndActivoTrue(chofer.getId())
                        .stream().findFirst();

                disponibilidad.add(ChoferDisponibilidadResponse.builder()
                        .choferId(chofer.getId())
                        .nombre(chofer.getNombres() + " " + chofer.getApellidos())
                        .cedula(chofer.getCedula())
                        .telefono(chofer.getTelefono())
                        .horasTrabajadasHoy(horasTrabajadas)
                        .horasDisponiblesHoy(Math.max(0, maxHorasNormal - horasTrabajadas))
                        .puedeTrabajarHorasExcepcionales(puedeExcepcional)
                        .diasExcepcionalesUsadosSemana((int) diasExcepcionalesUsados)
                        .frecuenciasHoy(frecuenciasRealizadas)
                        .disponible(disponible)
                        .motivoNoDisponible(motivoNoDisponible)
                        .busAsignadoId(asignacionBus.map(bc -> bc.getBus().getId()).orElse(null))
                        .busAsignadoPlaca(asignacionBus.map(bc -> bc.getBus().getPlaca()).orElse(null))
                        .build());
            }

            return disponibilidad;
        } catch (Exception e) {
            log.error("Error obteniendo disponibilidad de choferes para cooperativa {}: {}", cooperativaId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Valida si se puede crear una frecuencia con los parámetros dados
     */
    @Transactional(readOnly = true)
    public ValidacionFrecuenciaResponse validarFrecuencia(Long cooperativaId, CrearFrecuenciaValidadaRequest request, LocalDate fecha) {
        List<String> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();

        // Validar que los terminales pertenezcan a la cooperativa
        Set<Long> terminalIds = cooperativaTerminalRepository.findByCooperativaIdWithTerminal(cooperativaId)
                .stream()
                .map(ct -> ct.getTerminal().getId())
                .collect(Collectors.toSet());

        if (!terminalIds.contains(request.getTerminalOrigenId())) {
            errores.add("El terminal de origen no pertenece a la cooperativa");
        }
        if (!terminalIds.contains(request.getTerminalDestinoId())) {
            errores.add("El terminal de destino no pertenece a la cooperativa");
        }

        // Validar paradas intermedias
        if (request.getParadas() != null) {
            for (var parada : request.getParadas()) {
                if (!terminalIds.contains(parada.getTerminalId())) {
                    errores.add("La parada en terminal ID " + parada.getTerminalId() + " no pertenece a la cooperativa");
                }
            }
        }

        // Obtener disponibilidad del bus
        var busesDisponibles = getBusesDisponibles(cooperativaId, fecha);
        var busDisponibilidad = busesDisponibles.stream()
                .filter(b -> b.getBusId().equals(request.getBusId()))
                .findFirst()
                .orElse(null);

        if (busDisponibilidad == null) {
            errores.add("El bus seleccionado no existe o no pertenece a la cooperativa");
        } else if (!busDisponibilidad.getDisponible()) {
            errores.add("El bus no está disponible: " + busDisponibilidad.getMotivoNoDisponible());
        }

        // Validar chofer si se especificó
        ChoferDisponibilidadResponse choferDisponibilidad = null;
        if (request.getChoferId() != null) {
            var choferesDisponibles = getChoferesDisponibles(cooperativaId, fecha);
            choferDisponibilidad = choferesDisponibles.stream()
                    .filter(c -> c.getChoferId().equals(request.getChoferId()))
                    .findFirst()
                    .orElse(null);

            if (choferDisponibilidad == null) {
                errores.add("El chofer seleccionado no existe o no pertenece a la cooperativa");
            } else if (!choferDisponibilidad.getDisponible()) {
                errores.add("El chofer no está disponible: " + choferDisponibilidad.getMotivoNoDisponible());
            }
        } else {
            advertencias.add("No se ha seleccionado un chofer para esta frecuencia");
        }

        // Calcular precio y duración
        var config = configRepository.findByCooperativaId(cooperativaId).orElse(null);
        Terminal origen = terminalRepository.findById(request.getTerminalOrigenId()).orElse(null);
        Terminal destino = terminalRepository.findById(request.getTerminalDestinoId()).orElse(null);

        double distanciaKm = 0;
        int duracionMinutos = 0;
        double precioSugerido = 0;
        String horaLlegadaEstimada = null;

        if (origen != null && destino != null) {
            distanciaKm = calcularDistanciaAproximada(origen, destino);
            duracionMinutos = (int) (distanciaKm * 1.2);
            precioSugerido = config != null ? config.calcularPrecioSugerido(distanciaKm) : distanciaKm * 0.05;
            
            LocalTime horaSalida = LocalTime.parse(request.getHoraSalida());
            horaLlegadaEstimada = horaSalida.plusMinutes(duracionMinutos).toString();
        }

        return ValidacionFrecuenciaResponse.builder()
                .valida(errores.isEmpty())
                .errores(errores)
                .advertencias(advertencias)
                .busDisponibilidad(busDisponibilidad)
                .choferDisponibilidad(choferDisponibilidad)
                .precioSugerido(Math.round(precioSugerido * 100.0) / 100.0)
                .horaLlegadaEstimada(horaLlegadaEstimada)
                .duracionEstimadaMinutos(duracionMinutos)
                .build();
    }

    // Métodos auxiliares

    private double calcularDistanciaAproximada(Terminal origen, Terminal destino) {
        // Fórmula de Haversine para distancia entre coordenadas
        if (origen.getLatitud() == null || origen.getLongitud() == null ||
            destino.getLatitud() == null || destino.getLongitud() == null) {
            // Si no hay coordenadas, estimar basado en tipología
            return 100.0; // Default 100 km
        }

        double lat1 = Math.toRadians(origen.getLatitud());
        double lat2 = Math.toRadians(destino.getLatitud());
        double lon1 = Math.toRadians(origen.getLongitud());
        double lon2 = Math.toRadians(destino.getLongitud());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                   Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.asin(Math.sqrt(a));

        // Radio de la Tierra en km
        double r = 6371;

        // Multiplicar por factor de carretera (las rutas no son línea recta)
        return Math.round(r * c * 1.3 * 10.0) / 10.0;
    }

    private List<TerminalIntermedio> encontrarTerminalesIntermedios(
            Terminal origen, Terminal destino, List<Terminal> todosTerminales) {
        // Simplificado: encontrar terminales que estén entre origen y destino geográficamente
        List<TerminalIntermedio> intermedios = new ArrayList<>();
        
        // En una implementación real, se usaría una API de rutas para determinar
        // qué terminales están en el camino
        
        return intermedios;
    }
}
