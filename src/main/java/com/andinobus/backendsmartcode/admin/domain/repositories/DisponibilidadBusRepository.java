package com.andinobus.backendsmartcode.admin.domain.repositories;

import com.andinobus.backendsmartcode.admin.domain.entities.DisponibilidadBus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisponibilidadBusRepository extends JpaRepository<DisponibilidadBus, Long> {

    /**
     * Busca disponibilidades de un bus en una fecha específica
     */
    List<DisponibilidadBus> findByBusIdAndFecha(Long busId, LocalDate fecha);

    /**
     * Busca disponibilidades en una terminal para una fecha
     */
    List<DisponibilidadBus> findByTerminalIdAndFechaOrderByHoraDisponibleAsc(Long terminalId, LocalDate fecha);

    /**
     * Busca buses disponibles en una terminal a partir de una hora
     */
    @Query("SELECT d FROM DisponibilidadBus d " +
           "WHERE d.terminal.id = :terminalId " +
           "AND d.fecha = :fecha " +
           "AND d.horaDisponible <= :hora " +
           "AND d.estado = 'DISPONIBLE' " +
           "AND d.frecuenciaSiguiente IS NULL " +
           "ORDER BY d.horaDisponible ASC")
    List<DisponibilidadBus> findBusesDisponiblesEnTerminal(
        @Param("terminalId") Long terminalId,
        @Param("fecha") LocalDate fecha,
        @Param("hora") LocalTime hora
    );

    /**
     * Busca la próxima disponibilidad de un bus específico
     */
    @Query("SELECT d FROM DisponibilidadBus d " +
           "WHERE d.bus.id = :busId " +
           "AND d.fecha = :fecha " +
           "AND d.horaDisponible >= :horaMinima " +
           "AND d.frecuenciaSiguiente IS NULL " +
           "ORDER BY d.horaDisponible ASC")
    Optional<DisponibilidadBus> findProximaDisponibilidad(
        @Param("busId") Long busId,
        @Param("fecha") LocalDate fecha,
        @Param("horaMinima") LocalTime horaMinima
    );

    /**
     * Busca disponibilidades por cooperativa y fecha
     */
    List<DisponibilidadBus> findByCooperativaIdAndFechaOrderByHoraDisponibleAsc(Long cooperativaId, LocalDate fecha);

    /**
     * Cuenta buses disponibles en una terminal
     */
    @Query("SELECT COUNT(d) FROM DisponibilidadBus d " +
           "WHERE d.terminal.id = :terminalId " +
           "AND d.fecha = :fecha " +
           "AND d.estado = 'DISPONIBLE' " +
           "AND d.frecuenciaSiguiente IS NULL")
    long countBusesDisponiblesEnTerminal(
        @Param("terminalId") Long terminalId,
        @Param("fecha") LocalDate fecha
    );

    /**
     * Elimina disponibilidades de una cooperativa para un rango de fechas
     */
    void deleteByCooperativaIdAndFechaBetween(Long cooperativaId, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Elimina todas las disponibilidades de una cooperativa
     */
    void deleteByCooperativaId(Long cooperativaId);
}
