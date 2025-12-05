package com.andinobus.backendsmartcode.cooperativa.application.services;

import com.andinobus.backendsmartcode.admin.domain.entities.FrecuenciaViaje;
import com.andinobus.backendsmartcode.admin.domain.entities.Ruta;
import com.andinobus.backendsmartcode.admin.domain.repositories.FrecuenciaViajeRepository;
import com.andinobus.backendsmartcode.admin.domain.repositories.RutaRepository;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.catalogos.domain.entities.BusChofer;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusChoferRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusRepository;
import com.andinobus.backendsmartcode.cooperativa.api.dto.ReporteCooperativaDtos.*;
import com.andinobus.backendsmartcode.operacion.domain.entities.Viaje;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generación de reportes de cooperativa
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteCooperativaService {

    private final ViajeRepository viajeRepository;
    private final FrecuenciaViajeRepository frecuenciaViajeRepository;
    private final BusRepository busRepository;
    private final BusChoferRepository busChoferRepository;
    private final RutaRepository rutaRepository;

    /**
     * Obtiene el resumen general de la cooperativa
     */
    @Transactional(readOnly = true)
    public ResumenCooperativaResponse obtenerResumenGeneral(Long cooperativaId, LocalDate fechaInicio, LocalDate fechaFin) {
        log.info("Obteniendo resumen general para cooperativa {} desde {} hasta {}", 
                cooperativaId, fechaInicio, fechaFin);
        
        // Obtener recursos
        List<Bus> buses = busRepository.findByCooperativaIdAndActivoTrue(cooperativaId);
        List<BusChofer> busChoferes = busChoferRepository.findByCooperativaId(cooperativaId);
        List<FrecuenciaViaje> frecuencias = frecuenciaViajeRepository.findByBusCooperativaIdAndActivoTrue(cooperativaId);
        
        // Contar choferes únicos (un chofer puede estar en varios buses)
        Set<Long> choferesUnicos = busChoferes.stream()
                .map(bc -> bc.getChofer().getId())
                .collect(Collectors.toSet());
        
        // Contar rutas únicas de las frecuencias
        Set<Long> rutasUnicas = frecuencias.stream()
                .map(f -> f.getRuta().getId())
                .collect(Collectors.toSet());
        
        // Calcular estadísticas de viajes (simuladas por ahora)
        int totalViajes = frecuencias.size() * calcularDiasEnRango(fechaInicio, fechaFin);
        int viajesCompletados = (int) (totalViajes * 0.91); // 91% completados
        int viajesCancelados = (int) (totalViajes * 0.05);  // 5% cancelados
        int viajesPendientes = totalViajes - viajesCompletados - viajesCancelados;
        
        // Calcular ventas estimadas
        BigDecimal ventasEstimadas = calcularVentasEstimadas(frecuencias, fechaInicio, fechaFin);
        int totalTransacciones = (int) (totalViajes * 25); // Promedio 25 pasajeros por viaje
        BigDecimal ticketPromedio = totalTransacciones > 0 
                ? ventasEstimadas.divide(BigDecimal.valueOf(totalTransacciones), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        // Ocupación estimada
        double ocupacionPromedio = 78.5;
        double ocupacionMasAlta = 95.0;
        double ocupacionMasBaja = 45.0;
        
        return ResumenCooperativaResponse.builder()
                .ventasTotales(ventasEstimadas)
                .ventasCambio(BigDecimal.valueOf(12.5)) // Simulado
                .totalTransacciones(totalTransacciones)
                .ticketPromedio(ticketPromedio)
                .totalViajes(totalViajes)
                .viajesCompletados(viajesCompletados)
                .viajesCancelados(viajesCancelados)
                .viajesPendientes(viajesPendientes)
                .ocupacionPromedio(ocupacionPromedio)
                .ocupacionMasAlta(ocupacionMasAlta)
                .ocupacionMasBaja(ocupacionMasBaja)
                .asientosTotalesVendidos(totalTransacciones)
                .totalBuses(buses.size())
                .busesActivos((int) buses.stream().filter(b -> "DISPONIBLE".equals(b.getEstado()) || "EN_SERVICIO".equals(b.getEstado())).count())
                .totalChoferes(choferesUnicos.size())
                .choferesActivos(choferesUnicos.size()) // Todos activos por ahora
                .totalRutas(rutasUnicas.size())
                .rutasActivas(rutasUnicas.size())
                .build();
    }

    /**
     * Obtiene el reporte de ventas
     */
    @Transactional(readOnly = true)
    public ReporteVentasResponse obtenerReporteVentas(Long cooperativaId, LocalDate fechaInicio, LocalDate fechaFin) {
        log.info("Generando reporte de ventas para cooperativa {} desde {} hasta {}", 
                cooperativaId, fechaInicio, fechaFin);
        
        List<FrecuenciaViaje> frecuencias = frecuenciaViajeRepository.findByBusCooperativaIdAndActivoTrue(cooperativaId);
        
        // Generar ventas por día
        List<VentaDiariaDto> ventasPorDia = generarVentasPorDia(frecuencias, fechaInicio, fechaFin);
        
        // Calcular totales
        BigDecimal ventasTotales = ventasPorDia.stream()
                .map(VentaDiariaDto::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalTransacciones = ventasPorDia.stream()
                .mapToInt(VentaDiariaDto::getTransacciones)
                .sum();
        
        int diasEnRango = calcularDiasEnRango(fechaInicio, fechaFin);
        BigDecimal ventasDiarias = diasEnRango > 0 
                ? ventasTotales.divide(BigDecimal.valueOf(diasEnRango), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        BigDecimal ticketPromedio = totalTransacciones > 0
                ? ventasTotales.divide(BigDecimal.valueOf(totalTransacciones), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        // Top rutas por ventas
        List<RutaVentasDto> topRutas = generarTopRutasPorVentas(frecuencias, fechaInicio, fechaFin);
        
        return ReporteVentasResponse.builder()
                .ventasTotales(ventasTotales)
                .cambioVentas(BigDecimal.valueOf(12.5)) // Simulado
                .totalTransacciones(totalTransacciones)
                .ticketPromedio(ticketPromedio)
                .ventasDiarias(ventasDiarias)
                .ventasPorDia(ventasPorDia)
                .topRutas(topRutas)
                .build();
    }

    /**
     * Obtiene el reporte de viajes
     */
    @Transactional(readOnly = true)
    public ReporteViajesResponse obtenerReporteViajes(Long cooperativaId, LocalDate fechaInicio, LocalDate fechaFin) {
        log.info("Generando reporte de viajes para cooperativa {} desde {} hasta {}", 
                cooperativaId, fechaInicio, fechaFin);
        
        List<FrecuenciaViaje> frecuencias = frecuenciaViajeRepository.findByBusCooperativaIdAndActivoTrue(cooperativaId);
        
        int diasEnRango = calcularDiasEnRango(fechaInicio, fechaFin);
        int totalViajes = frecuencias.size() * diasEnRango;
        int viajesCompletados = (int) (totalViajes * 0.91);
        int viajesCancelados = (int) (totalViajes * 0.05);
        int viajesPendientes = (int) (totalViajes * 0.03);
        int viajesEnRuta = totalViajes - viajesCompletados - viajesCancelados - viajesPendientes;
        
        // Viajes por estado
        List<ViajeEstadoDto> viajesPorEstado = Arrays.asList(
                ViajeEstadoDto.builder().estado("COMPLETADO").cantidad(viajesCompletados)
                        .porcentaje(totalViajes > 0 ? (viajesCompletados * 100.0 / totalViajes) : 0).build(),
                ViajeEstadoDto.builder().estado("CANCELADO").cantidad(viajesCancelados)
                        .porcentaje(totalViajes > 0 ? (viajesCancelados * 100.0 / totalViajes) : 0).build(),
                ViajeEstadoDto.builder().estado("PENDIENTE").cantidad(viajesPendientes)
                        .porcentaje(totalViajes > 0 ? (viajesPendientes * 100.0 / totalViajes) : 0).build(),
                ViajeEstadoDto.builder().estado("EN_RUTA").cantidad(viajesEnRuta)
                        .porcentaje(totalViajes > 0 ? (viajesEnRuta * 100.0 / totalViajes) : 0).build()
        );
        
        // Viajes por día
        List<ViajeDiarioDto> viajesPorDia = generarViajesPorDia(frecuencias, fechaInicio, fechaFin);
        
        // Viajes por ruta
        List<ViajeRutaDto> viajesPorRuta = generarViajesPorRuta(frecuencias, diasEnRango);
        
        // Viajes por bus
        List<ViajeBusDto> viajesPorBus = generarViajesPorBus(frecuencias, diasEnRango);
        
        return ReporteViajesResponse.builder()
                .totalViajes(totalViajes)
                .viajesCompletados(viajesCompletados)
                .viajesCancelados(viajesCancelados)
                .viajesPendientes(viajesPendientes)
                .viajesEnRuta(viajesEnRuta)
                .porcentajeCompletados(totalViajes > 0 ? (viajesCompletados * 100.0 / totalViajes) : 0)
                .porcentajeCancelados(totalViajes > 0 ? (viajesCancelados * 100.0 / totalViajes) : 0)
                .viajesPorEstado(viajesPorEstado)
                .viajesPorDia(viajesPorDia)
                .viajesPorRuta(viajesPorRuta)
                .viajesPorBus(viajesPorBus)
                .build();
    }

    /**
     * Obtiene el reporte de ocupación
     */
    @Transactional(readOnly = true)
    public ReporteOcupacionResponse obtenerReporteOcupacion(Long cooperativaId, LocalDate fechaInicio, LocalDate fechaFin) {
        log.info("Generando reporte de ocupación para cooperativa {} desde {} hasta {}", 
                cooperativaId, fechaInicio, fechaFin);
        
        List<FrecuenciaViaje> frecuencias = frecuenciaViajeRepository.findByBusCooperativaIdAndActivoTrue(cooperativaId);
        List<Bus> buses = busRepository.findByCooperativaIdAndActivoTrue(cooperativaId);
        
        int diasEnRango = calcularDiasEnRango(fechaInicio, fechaFin);
        int capacidadPromedio = buses.isEmpty() ? 40 : 
                (int) buses.stream().mapToInt(b -> b.getCapacidadAsientos() != null ? b.getCapacidadAsientos() : 40).average().orElse(40);
        
        int totalViajes = frecuencias.size() * diasEnRango;
        int asientosTotales = totalViajes * capacidadPromedio;
        int asientosVendidos = (int) (asientosTotales * 0.785); // 78.5% ocupación promedio
        int asientosDisponibles = asientosTotales - asientosVendidos;
        
        // Ocupación por día
        List<OcupacionDiariaDto> ocupacionPorDia = generarOcupacionPorDia(frecuencias, fechaInicio, fechaFin, capacidadPromedio);
        
        // Ocupación por ruta
        List<OcupacionRutaDto> ocupacionPorRuta = generarOcupacionPorRuta(frecuencias, diasEnRango);
        
        // Ocupación por hora
        List<OcupacionHoraDto> ocupacionPorHora = generarOcupacionPorHora(frecuencias);
        
        return ReporteOcupacionResponse.builder()
                .ocupacionPromedio(78.5)
                .ocupacionMasAlta(95.0)
                .ocupacionMasBaja(45.0)
                .asientosTotales(asientosTotales)
                .asientosVendidos(asientosVendidos)
                .asientosDisponibles(asientosDisponibles)
                .ocupacionPorDia(ocupacionPorDia)
                .ocupacionPorRuta(ocupacionPorRuta)
                .ocupacionPorHora(ocupacionPorHora)
                .build();
    }

    /**
     * Obtiene el reporte de rutas
     */
    @Transactional(readOnly = true)
    public ReporteRutasResponse obtenerReporteRutas(Long cooperativaId, LocalDate fechaInicio, LocalDate fechaFin) {
        log.info("Generando reporte de rutas para cooperativa {} desde {} hasta {}", 
                cooperativaId, fechaInicio, fechaFin);
        
        List<FrecuenciaViaje> frecuencias = frecuenciaViajeRepository.findByBusCooperativaIdAndActivoTrue(cooperativaId);
        
        // Agrupar frecuencias por ruta
        Map<Long, List<FrecuenciaViaje>> frecuenciasPorRuta = frecuencias.stream()
                .collect(Collectors.groupingBy(f -> f.getRuta().getId()));
        
        int diasEnRango = calcularDiasEnRango(fechaInicio, fechaFin);
        
        List<DetalleRutaDto> rutas = frecuenciasPorRuta.entrySet().stream()
                .map(entry -> {
                    Ruta ruta = entry.getValue().get(0).getRuta();
                    List<FrecuenciaViaje> frecsRuta = entry.getValue();
                    int viajesRealizados = frecsRuta.size() * diasEnRango;
                    // Obtener precio de la primera frecuencia de esta ruta
                    Double precioFrecuencia = frecsRuta.stream()
                            .filter(f -> f.getPrecioBase() != null)
                            .findFirst()
                            .map(FrecuenciaViaje::getPrecioBase)
                            .orElse(5.0);
                    BigDecimal ingresos = BigDecimal.valueOf(viajesRealizados)
                            .multiply(BigDecimal.valueOf(25)) // 25 pasajeros promedio
                            .multiply(BigDecimal.valueOf(precioFrecuencia));
                    
                    return DetalleRutaDto.builder()
                            .rutaId(ruta.getId())
                            .terminalOrigen(ruta.getTerminalOrigen() != null ? ruta.getTerminalOrigen().getNombre() : "N/A")
                            .terminalDestino(ruta.getTerminalDestino() != null ? ruta.getTerminalDestino().getNombre() : "N/A")
                            .nombreRuta(ruta.getNombre())
                            .distanciaKm(ruta.getDistanciaKm())
                            .duracionMinutos(ruta.getDuracionEstimadaMinutos())
                            .precioBase(BigDecimal.valueOf(precioFrecuencia))
                            .frecuenciasActivas(frecsRuta.size())
                            .viajesRealizados(viajesRealizados)
                            .ingresosTotales(ingresos)
                            .ocupacionPromedio(75.0 + (Math.random() * 20)) // Simulado entre 75-95%
                            .activa(true)
                            .build();
                })
                .sorted((a, b) -> b.getIngresosTotales().compareTo(a.getIngresosTotales()))
                .collect(Collectors.toList());
        
        return ReporteRutasResponse.builder()
                .totalRutas(frecuenciasPorRuta.size())
                .rutasActivas(frecuenciasPorRuta.size())
                .frecuenciasActivas(frecuencias.size())
                .rutas(rutas)
                .build();
    }

    // ==================== MÉTODOS AUXILIARES ====================
    
    private int calcularDiasEnRango(LocalDate fechaInicio, LocalDate fechaFin) {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
    }
    
    private BigDecimal calcularVentasEstimadas(List<FrecuenciaViaje> frecuencias, LocalDate fechaInicio, LocalDate fechaFin) {
        int diasEnRango = calcularDiasEnRango(fechaInicio, fechaFin);
        int totalViajes = frecuencias.size() * diasEnRango;
        int pasajerosPromedio = 25;
        BigDecimal precioPromedio = frecuencias.isEmpty() ? BigDecimal.valueOf(5) :
                frecuencias.stream()
                        .map(f -> f.getPrecioBase() != null ? BigDecimal.valueOf(f.getPrecioBase()) : BigDecimal.valueOf(5))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(frecuencias.size()), 2, RoundingMode.HALF_UP);
        
        return BigDecimal.valueOf(totalViajes)
                .multiply(BigDecimal.valueOf(pasajerosPromedio))
                .multiply(precioPromedio);
    }
    
    private List<VentaDiariaDto> generarVentasPorDia(List<FrecuenciaViaje> frecuencias, LocalDate fechaInicio, LocalDate fechaFin) {
        List<VentaDiariaDto> ventas = new ArrayList<>();
        LocalDate fecha = fechaInicio;
        Random random = new Random();
        
        while (!fecha.isAfter(fechaFin)) {
            int viajesDia = frecuencias.size();
            int transacciones = viajesDia * (20 + random.nextInt(15)); // 20-35 pasajeros por viaje
            BigDecimal monto = BigDecimal.valueOf(transacciones * (4 + random.nextDouble() * 3)); // $4-7 por ticket
            
            ventas.add(VentaDiariaDto.builder()
                    .fecha(fecha)
                    .diaSemana(obtenerNombreDia(fecha.getDayOfWeek()))
                    .monto(monto.setScale(2, RoundingMode.HALF_UP))
                    .transacciones(transacciones)
                    .build());
            
            fecha = fecha.plusDays(1);
        }
        
        return ventas;
    }
    
    private List<RutaVentasDto> generarTopRutasPorVentas(List<FrecuenciaViaje> frecuencias, LocalDate fechaInicio, LocalDate fechaFin) {
        Map<Long, List<FrecuenciaViaje>> frecuenciasPorRuta = frecuencias.stream()
                .collect(Collectors.groupingBy(f -> f.getRuta().getId()));
        
        int diasEnRango = calcularDiasEnRango(fechaInicio, fechaFin);
        Random random = new Random();
        
        return frecuenciasPorRuta.entrySet().stream()
                .map(entry -> {
                    Ruta ruta = entry.getValue().get(0).getRuta();
                    List<FrecuenciaViaje> frecsRuta = entry.getValue();
                    int viajesRuta = frecsRuta.size() * diasEnRango;
                    int boletos = viajesRuta * (20 + random.nextInt(15));
                    // Obtener precio de la primera frecuencia
                    Double precioFrec = frecsRuta.stream()
                            .filter(f -> f.getPrecioBase() != null)
                            .findFirst()
                            .map(FrecuenciaViaje::getPrecioBase)
                            .orElse(5.0);
                    BigDecimal precio = BigDecimal.valueOf(precioFrec);
                    BigDecimal ventas = precio.multiply(BigDecimal.valueOf(boletos));
                    
                    String origen = ruta.getTerminalOrigen() != null ? ruta.getTerminalOrigen().getNombre() : "N/A";
                    String destino = ruta.getTerminalDestino() != null ? ruta.getTerminalDestino().getNombre() : "N/A";
                    
                    return RutaVentasDto.builder()
                            .rutaId(ruta.getId())
                            .nombreRuta(origen + " - " + destino)
                            .terminalOrigen(origen)
                            .terminalDestino(destino)
                            .ventas(ventas.setScale(2, RoundingMode.HALF_UP))
                            .boletos(boletos)
                            .build();
                })
                .sorted((a, b) -> b.getVentas().compareTo(a.getVentas()))
                .limit(5)
                .collect(Collectors.toList());
    }
    
    private List<ViajeDiarioDto> generarViajesPorDia(List<FrecuenciaViaje> frecuencias, LocalDate fechaInicio, LocalDate fechaFin) {
        List<ViajeDiarioDto> viajes = new ArrayList<>();
        LocalDate fecha = fechaInicio;
        Random random = new Random();
        
        while (!fecha.isAfter(fechaFin)) {
            int total = frecuencias.size();
            int completados = (int) (total * (0.88 + random.nextDouble() * 0.1)); // 88-98%
            int cancelados = (int) (total * random.nextDouble() * 0.08); // 0-8%
            
            viajes.add(ViajeDiarioDto.builder()
                    .fecha(fecha)
                    .diaSemana(obtenerNombreDia(fecha.getDayOfWeek()))
                    .total(total)
                    .completados(completados)
                    .cancelados(cancelados)
                    .build());
            
            fecha = fecha.plusDays(1);
        }
        
        return viajes;
    }
    
    private List<ViajeRutaDto> generarViajesPorRuta(List<FrecuenciaViaje> frecuencias, int diasEnRango) {
        Map<Long, List<FrecuenciaViaje>> frecuenciasPorRuta = frecuencias.stream()
                .collect(Collectors.groupingBy(f -> f.getRuta().getId()));
        
        Random random = new Random();
        
        return frecuenciasPorRuta.entrySet().stream()
                .map(entry -> {
                    Ruta ruta = entry.getValue().get(0).getRuta();
                    int totalViajes = entry.getValue().size() * diasEnRango;
                    int completados = (int) (totalViajes * (0.88 + random.nextDouble() * 0.1));
                    
                    String origen = ruta.getTerminalOrigen() != null ? ruta.getTerminalOrigen().getNombre() : "N/A";
                    String destino = ruta.getTerminalDestino() != null ? ruta.getTerminalDestino().getNombre() : "N/A";
                    
                    return ViajeRutaDto.builder()
                            .rutaId(ruta.getId())
                            .nombreRuta(origen + " - " + destino)
                            .totalViajes(totalViajes)
                            .viajesCompletados(completados)
                            .porcentajeOcupacion(70 + random.nextDouble() * 25) // 70-95%
                            .build();
                })
                .sorted((a, b) -> Integer.compare(b.getTotalViajes(), a.getTotalViajes()))
                .collect(Collectors.toList());
    }
    
    private List<ViajeBusDto> generarViajesPorBus(List<FrecuenciaViaje> frecuencias, int diasEnRango) {
        Map<Long, List<FrecuenciaViaje>> frecuenciasPorBus = frecuencias.stream()
                .collect(Collectors.groupingBy(f -> f.getBus().getId()));
        
        Random random = new Random();
        
        return frecuenciasPorBus.entrySet().stream()
                .map(entry -> {
                    Bus bus = entry.getValue().get(0).getBus();
                    int totalViajes = entry.getValue().size() * diasEnRango;
                    int completados = (int) (totalViajes * (0.88 + random.nextDouble() * 0.1));
                    double horas = totalViajes * (1.5 + random.nextDouble() * 2); // 1.5-3.5 horas por viaje
                    
                    return ViajeBusDto.builder()
                            .busId(bus.getId())
                            .placa(bus.getPlaca())
                            .totalViajes(totalViajes)
                            .viajesCompletados(completados)
                            .horasTrabajadas(Math.round(horas * 10.0) / 10.0)
                            .build();
                })
                .sorted((a, b) -> Integer.compare(b.getTotalViajes(), a.getTotalViajes()))
                .collect(Collectors.toList());
    }
    
    private List<OcupacionDiariaDto> generarOcupacionPorDia(List<FrecuenciaViaje> frecuencias, 
            LocalDate fechaInicio, LocalDate fechaFin, int capacidadPromedio) {
        List<OcupacionDiariaDto> ocupacion = new ArrayList<>();
        LocalDate fecha = fechaInicio;
        Random random = new Random();
        
        while (!fecha.isAfter(fechaFin)) {
            int viajesDia = frecuencias.size();
            int asientosTotales = viajesDia * capacidadPromedio;
            double porcentaje = 65 + random.nextDouble() * 30; // 65-95%
            int vendidos = (int) (asientosTotales * porcentaje / 100);
            
            ocupacion.add(OcupacionDiariaDto.builder()
                    .fecha(fecha)
                    .diaSemana(obtenerNombreDia(fecha.getDayOfWeek()))
                    .porcentaje(Math.round(porcentaje * 10.0) / 10.0)
                    .asientosVendidos(vendidos)
                    .asientosTotales(asientosTotales)
                    .build());
            
            fecha = fecha.plusDays(1);
        }
        
        return ocupacion;
    }
    
    private List<OcupacionRutaDto> generarOcupacionPorRuta(List<FrecuenciaViaje> frecuencias, int diasEnRango) {
        Map<Long, List<FrecuenciaViaje>> frecuenciasPorRuta = frecuencias.stream()
                .collect(Collectors.groupingBy(f -> f.getRuta().getId()));
        
        Random random = new Random();
        
        return frecuenciasPorRuta.entrySet().stream()
                .map(entry -> {
                    Ruta ruta = entry.getValue().get(0).getRuta();
                    String origen = ruta.getTerminalOrigen() != null ? ruta.getTerminalOrigen().getNombre() : "N/A";
                    String destino = ruta.getTerminalDestino() != null ? ruta.getTerminalDestino().getNombre() : "N/A";
                    
                    return OcupacionRutaDto.builder()
                            .nombreRuta(origen + " - " + destino)
                            .ocupacionPromedio(Math.round((70 + random.nextDouble() * 25) * 10.0) / 10.0)
                            .viajes(entry.getValue().size() * diasEnRango)
                            .build();
                })
                .sorted((a, b) -> Double.compare(b.getOcupacionPromedio(), a.getOcupacionPromedio()))
                .collect(Collectors.toList());
    }
    
    private List<OcupacionHoraDto> generarOcupacionPorHora(List<FrecuenciaViaje> frecuencias) {
        // Agrupar frecuencias por hora de salida
        Map<Integer, Long> frecuenciasPorHora = frecuencias.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getHoraSalida().getHour(),
                        Collectors.counting()
                ));
        
        Random random = new Random();
        
        return frecuenciasPorHora.entrySet().stream()
                .map(entry -> {
                    // Ocupación más alta en horas pico (6-9, 17-20)
                    int hora = entry.getKey();
                    double ocupacionBase = 70;
                    if ((hora >= 6 && hora <= 9) || (hora >= 17 && hora <= 20)) {
                        ocupacionBase = 85;
                    } else if (hora >= 10 && hora <= 16) {
                        ocupacionBase = 75;
                    } else {
                        ocupacionBase = 60;
                    }
                    
                    return OcupacionHoraDto.builder()
                            .hora(hora)
                            .ocupacionPromedio(Math.round((ocupacionBase + random.nextDouble() * 10) * 10.0) / 10.0)
                            .viajes(entry.getValue().intValue())
                            .build();
                })
                .sorted(Comparator.comparingInt(OcupacionHoraDto::getHora))
                .collect(Collectors.toList());
    }
    
    private String obtenerNombreDia(DayOfWeek dayOfWeek) {
        return dayOfWeek.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
    }
}
