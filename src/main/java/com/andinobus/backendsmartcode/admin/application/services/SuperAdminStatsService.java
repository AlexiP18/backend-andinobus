package com.andinobus.backendsmartcode.admin.application.services;

import com.andinobus.backendsmartcode.admin.api.dto.SuperAdminDtos;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.CooperativaRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusRepository;
import com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories.UsuarioCooperativaRepository;
import com.andinobus.backendsmartcode.usuarios.domain.entities.AppUser;
import com.andinobus.backendsmartcode.usuarios.domain.repositories.UserRepository;
import com.andinobus.backendsmartcode.operacion.domain.entities.Viaje;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeRepository;
import com.andinobus.backendsmartcode.ventas.domain.entities.Reserva;
import com.andinobus.backendsmartcode.ventas.domain.repositories.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Profile("dev")
@Service
@RequiredArgsConstructor
public class SuperAdminStatsService {

    private final CooperativaRepository cooperativaRepository;
    private final BusRepository busRepository;
    private final UsuarioCooperativaRepository usuarioCooperativaRepository;
    private final UserRepository userRepository;
    private final ViajeRepository viajeRepository;
    private final ReservaRepository reservaRepository;

    @Transactional(readOnly = true)
    public SuperAdminDtos.SuperAdminStatsResponse getStats() {
        log.info("Obteniendo estadísticas globales del sistema");

        // Estadísticas de cooperativas
        List<Cooperativa> todasCooperativas = cooperativaRepository.findAll();
        int totalCooperativas = todasCooperativas.size();
        int cooperativasActivas = (int) todasCooperativas.stream()
                .filter(Cooperativa::getActivo)
                .count();

        // Estadísticas de buses
        List<Bus> todosBuses = busRepository.findAll();
        int totalBuses = todosBuses.size();
        int busesActivos = (int) todosBuses.stream()
                .filter(Bus::getActivo)
                .count();

        // Estadísticas de usuarios
        long totalUsuarios = usuarioCooperativaRepository.count();
        long usuariosActivos = usuarioCooperativaRepository.findAll().stream()
                .filter(u -> u.getActivo())
                .count();

        // Estadísticas de viajes de hoy
        LocalDate hoy = LocalDate.now();
        List<Viaje> viajesHoy = viajeRepository.findByFecha(hoy);
        int cantidadViajesHoy = viajesHoy.size();

        // Estadísticas de reservas y ventas de hoy
        List<Reserva> reservasHoy = reservaRepository.findAll().stream()
                .filter(r -> viajesHoy.stream().anyMatch(v -> v.getId().equals(r.getViaje().getId())))
                .collect(Collectors.toList());

        double ventasTotalesHoy = reservasHoy.stream()
                .filter(r -> "PAGADO".equals(r.getEstado()))
                .mapToDouble(r -> r.getMonto() != null ? r.getMonto().doubleValue() : 0.0)
                .sum();

        int reservasPendientes = (int) reservasHoy.stream()
                .filter(r -> "PENDIENTE".equals(r.getEstado()))
                .count();

        return SuperAdminDtos.SuperAdminStatsResponse.builder()
                .totalCooperativas(totalCooperativas)
                .cooperativasActivas(cooperativasActivas)
                .totalBuses(totalBuses)
                .busesActivos(busesActivos)
                .totalUsuarios((int) totalUsuarios)
                .usuariosActivos((int) usuariosActivos)
                .ventasTotalesHoy(ventasTotalesHoy)
                .viajesHoy(cantidadViajesHoy)
                .reservasPendientes(reservasPendientes)
                .build();
    }

    @Transactional(readOnly = true)
    public List<SuperAdminDtos.CooperativaInfo> getAllCooperativas() {
        log.info("Obteniendo lista completa de cooperativas");

        return cooperativaRepository.findAll().stream()
                .map(coop -> {
                    int cantidadBuses = busRepository.findByCooperativaId(coop.getId()).size();
                    int cantidadPersonal = usuarioCooperativaRepository.findByCooperativaId(coop.getId()).size();

                    return SuperAdminDtos.CooperativaInfo.builder()
                            .id(coop.getId())
                            .nombre(coop.getNombre())
                            .ruc(coop.getRuc())
                            .logoUrl(coop.getLogoUrl())
                            .cantidadBuses(cantidadBuses)
                            .cantidadPersonal(cantidadPersonal)
                            .activo(coop.getActivo())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SuperAdminDtos.CooperativaDetalleResponse getCooperativaDetalle(Long cooperativaId) {
        log.info("Obteniendo detalle de cooperativa ID: {}", cooperativaId);

        Cooperativa coop = cooperativaRepository.findById(cooperativaId)
                .orElseThrow(() -> new RuntimeException("Cooperativa no encontrada con ID: " + cooperativaId));

        // Obtener buses
        List<SuperAdminDtos.BusInfo> buses = busRepository.findByCooperativaId(cooperativaId).stream()
                .map(bus -> {
                    String modelo = bus.getChasisMarca() != null && bus.getCarroceriaMarca() != null 
                        ? bus.getChasisMarca() + " / " + bus.getCarroceriaMarca()
                        : (bus.getChasisMarca() != null ? bus.getChasisMarca() : "N/A");
                    
                    return SuperAdminDtos.BusInfo.builder()
                            .id(bus.getId())
                            .placa(bus.getPlaca())
                            .modelo(modelo)
                            .capacidad(bus.getCapacidadAsientos() != null ? bus.getCapacidadAsientos() : 0)
                            .activo(bus.getActivo())
                            .build();
                })
                .collect(Collectors.toList());

        // Obtener usuarios
        List<SuperAdminDtos.UsuarioInfo> usuarios = usuarioCooperativaRepository.findByCooperativaId(cooperativaId).stream()
                .map(usuario -> SuperAdminDtos.UsuarioInfo.builder()
                        .id(usuario.getId())
                        .nombres(usuario.getNombres())
                        .apellidos(usuario.getApellidos())
                        .email(usuario.getEmail())
                        .rol(usuario.getRolCooperativa().name())
                        .activo(usuario.getActivo())
                        .build())
                .collect(Collectors.toList());

        // Calcular estadísticas específicas de la cooperativa
        int totalBuses = buses.size();
        int busesActivos = (int) buses.stream().filter(SuperAdminDtos.BusInfo::isActivo).count();
        int totalUsuarios = usuarios.size();
        int usuariosActivos = (int) usuarios.stream().filter(SuperAdminDtos.UsuarioInfo::isActivo).count();

        // Viajes y ventas de hoy para esta cooperativa
        LocalDate hoy = LocalDate.now();
        List<Viaje> viajesHoyCooperativa = viajeRepository.findByFecha(hoy).stream()
                .filter(v -> v.getBus().getCooperativa().getId().equals(cooperativaId))
                .collect(Collectors.toList());

        int viajesHoy = viajesHoyCooperativa.size();

        List<Reserva> reservasHoyCooperativa = reservaRepository.findAll().stream()
                .filter(r -> viajesHoyCooperativa.stream().anyMatch(v -> v.getId().equals(r.getViaje().getId())))
                .collect(Collectors.toList());

        double ventasHoy = reservasHoyCooperativa.stream()
                .filter(r -> "PAGADO".equals(r.getEstado()))
                .mapToDouble(r -> r.getMonto() != null ? r.getMonto().doubleValue() : 0.0)
                .sum();

        int reservasPendientes = (int) reservasHoyCooperativa.stream()
                .filter(r -> "PENDIENTE".equals(r.getEstado()))
                .count();

        SuperAdminDtos.CooperativaStatsResponse stats = SuperAdminDtos.CooperativaStatsResponse.builder()
                .totalBuses(totalBuses)
                .busesActivos(busesActivos)
                .totalUsuarios(totalUsuarios)
                .usuariosActivos(usuariosActivos)
                .viajesHoy(viajesHoy)
                .ventasHoy(ventasHoy)
                .reservasPendientes(reservasPendientes)
                .build();

        return SuperAdminDtos.CooperativaDetalleResponse.builder()
                .id(coop.getId())
                .nombre(coop.getNombre())
                .ruc(coop.getRuc())
                .direccion(null) // Campo no existe en Cooperativa
                .telefono(null)  // Campo no existe en Cooperativa
                .email(null)     // Campo no existe en Cooperativa
                .activo(coop.getActivo())
                .estadisticas(stats)
                .buses(buses)
                .usuarios(usuarios)
                .build();
    }

    /**
     * Activa o desactiva una cooperativa
     */
    @Transactional
    public void toggleCooperativaEstado(Long cooperativaId, boolean activo) {
        log.info("Cambiando estado de cooperativa {} a {}", cooperativaId, activo ? "ACTIVA" : "INACTIVA");
        
        Cooperativa cooperativa = cooperativaRepository.findById(cooperativaId)
                .orElseThrow(() -> new RuntimeException("Cooperativa no encontrada con ID: " + cooperativaId));
        
        cooperativa.setActivo(activo);
        cooperativaRepository.save(cooperativa);
        
        log.info("Estado de cooperativa {} actualizado exitosamente", cooperativaId);
    }

    /**
     * Obtiene la lista de todos los clientes
     */
    @Transactional(readOnly = true)
    public List<SuperAdminDtos.ClienteInfo> getAllClientes() {
        log.info("Obteniendo lista completa de clientes");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        return userRepository.findAll().stream()
                .map(cliente -> SuperAdminDtos.ClienteInfo.builder()
                        .id(cliente.getId())
                        .email(cliente.getEmail())
                        .nombres(cliente.getNombres())
                        .apellidos(cliente.getApellidos())
                        .activo(cliente.getActivo())
                        .createdAt(cliente.getCreatedAt() != null ? cliente.getCreatedAt().format(formatter) : "")
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Activa o desactiva un cliente
     */
    @Transactional
    public void toggleClienteEstado(Long clienteId, boolean activo) {
        log.info("Cambiando estado de cliente {} a {}", clienteId, activo ? "ACTIVO" : "INACTIVO");
        
        AppUser cliente = userRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + clienteId));
        
        cliente.setActivo(activo);
        userRepository.save(cliente);
        
        log.info("Estado de cliente {} actualizado exitosamente", clienteId);
    }
    
    /**
     * Obtiene el reporte de ventas globales (todas las cooperativas) para un rango de fechas
     */
    @Transactional(readOnly = true)
    public SuperAdminDtos.ReporteVentasGlobalResponse getReporteVentasGlobal(LocalDate fechaInicio, LocalDate fechaFin) {
        log.info("Generando reporte de ventas global desde {} hasta {}", fechaInicio, fechaFin);
        
        // Obtener ventas totales globales
        java.math.BigDecimal ventasTotales = reservaRepository.sumVentasTotalesGlobal(fechaInicio, fechaFin);
        if (ventasTotales == null) ventasTotales = java.math.BigDecimal.ZERO;
        
        long totalTransacciones = reservaRepository.countTransaccionesGlobal(fechaInicio, fechaFin);
        
        // Obtener ventas por día
        List<Object[]> ventasPorDiaRaw = reservaRepository.findVentasPorDiaGlobal(fechaInicio, fechaFin);
        List<SuperAdminDtos.VentaDiariaGlobal> ventasPorDia = ventasPorDiaRaw.stream()
                .map(row -> SuperAdminDtos.VentaDiariaGlobal.builder()
                        .fecha((LocalDate) row[0])
                        .monto((java.math.BigDecimal) row[1])
                        .transacciones(((Long) row[2]).intValue())
                        .build())
                .collect(Collectors.toList());
        
        // Obtener ventas por cooperativa
        List<Object[]> ventasPorCooperativaRaw = reservaRepository.findVentasPorCooperativa(fechaInicio, fechaFin);
        List<SuperAdminDtos.VentaCooperativa> ventasPorCooperativa = ventasPorCooperativaRaw.stream()
                .map(row -> SuperAdminDtos.VentaCooperativa.builder()
                        .cooperativaId((Long) row[0])
                        .cooperativaNombre((String) row[1])
                        .ventas((java.math.BigDecimal) row[2])
                        .transacciones(((Long) row[3]).intValue())
                        .build())
                .collect(Collectors.toList());
        
        // Calcular ticket promedio
        java.math.BigDecimal ticketPromedio = totalTransacciones > 0
                ? ventasTotales.divide(java.math.BigDecimal.valueOf(totalTransacciones), 2, java.math.RoundingMode.HALF_UP)
                : java.math.BigDecimal.ZERO;
        
        return SuperAdminDtos.ReporteVentasGlobalResponse.builder()
                .ventasTotales(ventasTotales)
                .totalTransacciones((int) totalTransacciones)
                .ticketPromedio(ticketPromedio)
                .ventasPorDia(ventasPorDia)
                .ventasPorCooperativa(ventasPorCooperativa)
                .build();
    }
}
