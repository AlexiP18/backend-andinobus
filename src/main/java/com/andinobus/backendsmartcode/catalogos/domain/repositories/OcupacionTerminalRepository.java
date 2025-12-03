package com.andinobus.backendsmartcode.catalogos.domain.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.entities.OcupacionTerminal;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OcupacionTerminalRepository extends JpaRepository<OcupacionTerminal, Long> {

    Optional<OcupacionTerminal> findByTerminalAndFechaAndHora(
            Terminal terminal, LocalDate fecha, LocalTime hora);

    @Query("SELECT o FROM OcupacionTerminal o WHERE o.terminal.id = :terminalId AND o.fecha = :fecha ORDER BY o.hora")
    List<OcupacionTerminal> findByTerminalIdAndFecha(
            @Param("terminalId") Long terminalId, 
            @Param("fecha") LocalDate fecha);

    @Query("SELECT SUM(o.frecuenciasAsignadas) FROM OcupacionTerminal o " +
           "WHERE o.terminal.id = :terminalId AND o.fecha = :fecha")
    Long getTotalFrecuenciasDelDia(
            @Param("terminalId") Long terminalId, 
            @Param("fecha") LocalDate fecha);

    @Query("SELECT o FROM OcupacionTerminal o " +
           "WHERE o.terminal.id = :terminalId AND o.fecha = :fecha " +
           "AND o.hora BETWEEN :horaInicio AND :horaFin")
    List<OcupacionTerminal> findByTerminalIdAndFechaAndRangoHora(
            @Param("terminalId") Long terminalId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin);

    @Query("SELECT COUNT(o) FROM OcupacionTerminal o " +
           "WHERE o.terminal.id = :terminalId AND o.fecha = :fecha " +
           "AND o.frecuenciasAsignadas >= :limite")
    long countHorasSaturadas(
            @Param("terminalId") Long terminalId,
            @Param("fecha") LocalDate fecha,
            @Param("limite") int limite);

    /**
     * Obtiene las horas con disponibilidad para un terminal en una fecha
     */
    @Query("SELECT o.hora FROM OcupacionTerminal o " +
           "WHERE o.terminal.id = :terminalId AND o.fecha = :fecha " +
           "AND o.frecuenciasAsignadas < :maxPorHora ORDER BY o.hora")
    List<LocalTime> findHorasDisponibles(
            @Param("terminalId") Long terminalId,
            @Param("fecha") LocalDate fecha,
            @Param("maxPorHora") int maxPorHora);

    void deleteByTerminalAndFecha(Terminal terminal, LocalDate fecha);
}
