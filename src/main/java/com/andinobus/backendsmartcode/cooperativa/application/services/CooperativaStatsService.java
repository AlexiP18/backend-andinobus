package com.andinobus.backendsmartcode.cooperativa.application.services;

import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusRepository;
import com.andinobus.backendsmartcode.cooperativa.api.dto.CooperativaStatsDtos;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.UsuarioCooperativa;
import com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories.UsuarioCooperativaRepository;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeRepository;
import com.andinobus.backendsmartcode.ventas.domain.repositories.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CooperativaStatsService {

    private final BusRepository busRepository;
    private final UsuarioCooperativaRepository usuarioCooperativaRepository;
    private final ViajeRepository viajeRepository;
    private final ReservaRepository reservaRepository;

    @Transactional(readOnly = true)
    public CooperativaStatsDtos.CooperativaStats getCooperativaStats(Long cooperativaId) {
        int totalBuses = busRepository.countByCooperativaId(cooperativaId);
        int busesActivos = busRepository.countByCooperativaIdAndActivoTrue(cooperativaId);
        int totalPersonal = usuarioCooperativaRepository.countByCooperativaIdAndActivoTrue(cooperativaId);

        return CooperativaStatsDtos.CooperativaStats.builder()
                .cooperativaId(cooperativaId)
                .totalBuses(totalBuses)
                .busesActivos(busesActivos)
                .totalPersonal(totalPersonal)
                .ventasDelMes(BigDecimal.ZERO) // TODO: Calcular desde reservas
                .ventasDeHoy(BigDecimal.ZERO)
                .build();
    }

    @Transactional(readOnly = true)
    public CooperativaStatsDtos.AdminStats getAdminStats(Long cooperativaId) {
        int busesActivos = busRepository.countByCooperativaIdAndActivoTrue(cooperativaId);
        int totalPersonal = usuarioCooperativaRepository.countByCooperativaIdAndActivoTrue(cooperativaId);
        
        // Contar choferes y oficinistas activos
        var personal = usuarioCooperativaRepository.findByCooperativaIdAndActivoTrue(cooperativaId);
        int choferes = (int) personal.stream()
                .filter(p -> "CHOFER".equals(p.getRolCooperativa().name()))
                .count();
        int oficinistas = (int) personal.stream()
                .filter(p -> "OFICINISTA".equals(p.getRolCooperativa().name()))
                .count();

        // Contar viajes de hoy
        LocalDate hoy = LocalDate.now();
        long viajesHoyLong = viajeRepository.countByFechaAndFrecuenciaCooperativaId(hoy, cooperativaId);
        
        // Calcular ventas de hoy
        double ventasHoyDouble = reservaRepository.sumTotalPagadoByCooperativaIdAndFecha(cooperativaId, hoy);

        return CooperativaStatsDtos.AdminStats.builder()
                .busesActivos(busesActivos)
                .totalPersonal(totalPersonal)
                .choferes(choferes)
                .oficinistas(oficinistas)
                .ventasHoy(BigDecimal.valueOf(ventasHoyDouble))
                .viajesHoy((int) viajesHoyLong)
                .build();
    }

    @Transactional(readOnly = true)
    public CooperativaStatsDtos.OficinistaStats getOficinistaStats(Long cooperativaId) {
        LocalDate hoy = LocalDate.now();
        
        // Contar reservas pagadas del día
        long boletosVendidosHoyLong = reservaRepository.countByCooperativaIdAndFechaAndEstado(cooperativaId, hoy, "PAGADO");
        
        // Contar reservas pendientes de la cooperativa
        long reservasPendientesLong = reservaRepository.countPendientesByCooperativaId(cooperativaId);
        
        // Contar viajes programados para hoy
        long viajesProgramadosLong = viajeRepository.countByFechaAndFrecuenciaCooperativaIdAndEstadoProgramado(hoy, cooperativaId);
        
        // Calcular recaudado de hoy
        double recaudadoHoyDouble = reservaRepository.sumTotalPagadoByCooperativaIdAndFecha(cooperativaId, hoy);

        return CooperativaStatsDtos.OficinistaStats.builder()
                .boletosVendidosHoy((int) boletosVendidosHoyLong)
                .recaudadoHoy(BigDecimal.valueOf(recaudadoHoyDouble))
                .reservasPendientes((int) reservasPendientesLong)
                .viajesProgramados((int) viajesProgramadosLong)
                .pasajerosRegistrados(0) // TODO: Calcular desde asientos ocupados
                .build();
    }

    @Transactional(readOnly = true)
    public CooperativaStatsDtos.ChoferStats getChoferStats(Long choferId) {
        // Buscar chofer
        UsuarioCooperativa chofer = usuarioCooperativaRepository.findById(choferId)
                .orElseThrow(() -> new RuntimeException("Chofer no encontrado"));

        // Contar viajes del mes
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        long viajesDelMesLong = viajeRepository.countByChoferIdAndFechaBetween(choferId, inicioMes, finMes);

        // Buscar viaje actual (hoy, asignado al chofer)
        LocalDate hoy = LocalDate.now();
        var viajesActuales = viajeRepository.findByChoferIdAndFechaAndEstadoActivo(choferId, hoy);
        var viajeActual = viajesActuales.isEmpty() ? null : viajesActuales.get(0);

        CooperativaStatsDtos.ViajeActual viajeActualDto = null;
        if (viajeActual != null) {
            viajeActualDto = CooperativaStatsDtos.ViajeActual.builder()
                    .viajeId(viajeActual.getId())
                    .origen(viajeActual.getFrecuencia().getOrigen())
                    .destino(viajeActual.getFrecuencia().getDestino())
                    .fecha(viajeActual.getFecha())
                    .horaSalida(viajeActual.getHoraSalidaProgramada())
                    .busPlaca(viajeActual.getBus() != null ? viajeActual.getBus().getPlaca() : "N/A")
                    .pasajerosConfirmados(0) // TODO: Contar desde asientos
                    .estado(viajeActual.getEstado())
                    .build();
        }

        return CooperativaStatsDtos.ChoferStats.builder()
                .viajesDelMes((int) viajesDelMesLong)
                .pasajerosTransportados(0) // TODO: Calcular
                .calificacion(chofer.getCalificacion() != null ? chofer.getCalificacion() : BigDecimal.ZERO)
                .viajeActual(viajeActualDto)
                .build();
    }

    @Transactional(readOnly = true)
    public CooperativaStatsDtos.BusesList getBuses(Long cooperativaId) {
        var buses = busRepository.findByCooperativaId(cooperativaId);
        
        var busInfoList = buses.stream()
                .map(bus -> CooperativaStatsDtos.BusInfo.builder()
                        .id(bus.getId())
                        .placa(bus.getPlaca())
                        .modelo(bus.getChasisMarca() + " / " + bus.getCarroceriaMarca())
                        .capacidad(bus.getCapacidadAsientos())
                        .estado(bus.getActivo() ? "ACTIVO" : "INACTIVO")
                        .anioFabricacion(null) // No hay campo en la entidad
                        .build())
                .collect(Collectors.toList());

        return CooperativaStatsDtos.BusesList.builder()
                .buses(busInfoList)
                .total(busInfoList.size())
                .build();
    }

    @Transactional(readOnly = true)
    public CooperativaStatsDtos.PersonalList getPersonal(Long cooperativaId) {
        var personal = usuarioCooperativaRepository.findByCooperativaId(cooperativaId);
        
        var personalInfoList = personal.stream()
                .map(p -> CooperativaStatsDtos.PersonalInfo.builder()
                        .id(p.getId())
                        .nombres(p.getNombres())
                        .apellidos(p.getApellidos())
                        .email(p.getEmail())
                        .rolCooperativa(p.getRolCooperativa().name())
                        .cedula(p.getCedula())
                        .telefono(p.getTelefono())
                        .activo(p.getActivo())
                        .build())
                .collect(Collectors.toList());

        return CooperativaStatsDtos.PersonalList.builder()
                .personal(personalInfoList)
                .total(personalInfoList.size())
                .build();
    }
}
