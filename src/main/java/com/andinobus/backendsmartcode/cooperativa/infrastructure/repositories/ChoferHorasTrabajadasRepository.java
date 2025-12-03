package com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories;

import com.andinobus.backendsmartcode.cooperativa.domain.entities.ChoferHorasTrabajadas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChoferHorasTrabajadasRepository extends JpaRepository<ChoferHorasTrabajadas, Long> {

    Optional<ChoferHorasTrabajadas> findByChoferIdAndFecha(Long choferId, LocalDate fecha);

    List<ChoferHorasTrabajadas> findByChoferIdAndFechaBetween(Long choferId, LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT COALESCE(SUM(h.horasTrabajadas), 0) FROM ChoferHorasTrabajadas h " +
           "WHERE h.chofer.id = :choferId AND h.fecha BETWEEN :fechaInicio AND :fechaFin")
    Double sumHorasByChoferAndFechaRange(@Param("choferId") Long choferId, 
                                          @Param("fechaInicio") LocalDate fechaInicio,
                                          @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT COUNT(h) FROM ChoferHorasTrabajadas h " +
           "WHERE h.chofer.id = :choferId AND h.horasExcepcionales = true " +
           "AND h.fecha BETWEEN :fechaInicio AND :fechaFin")
    Long countDiasExcepcionalesBySemana(@Param("choferId") Long choferId,
                                         @Param("fechaInicio") LocalDate fechaInicio,
                                         @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT h FROM ChoferHorasTrabajadas h " +
           "WHERE h.chofer.cooperativa.id = :cooperativaId AND h.fecha = :fecha")
    List<ChoferHorasTrabajadas> findByCooperativaAndFecha(@Param("cooperativaId") Long cooperativaId,
                                                          @Param("fecha") LocalDate fecha);
}
