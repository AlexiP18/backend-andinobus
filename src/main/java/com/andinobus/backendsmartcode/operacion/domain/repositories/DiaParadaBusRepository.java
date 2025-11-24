package com.andinobus.backendsmartcode.operacion.domain.repositories;

import com.andinobus.backendsmartcode.operacion.domain.entities.DiaParadaBus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaParadaBusRepository extends JpaRepository<DiaParadaBus, Long> {
    
    List<DiaParadaBus> findByBusId(Long busId);
    
    List<DiaParadaBus> findByFecha(LocalDate fecha);
    
    Optional<DiaParadaBus> findByBusIdAndFecha(Long busId, LocalDate fecha);
    
    @Query("SELECT d FROM DiaParadaBus d WHERE d.bus.id = :busId " +
           "AND d.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY d.fecha")
    List<DiaParadaBus> findByBusIdAndFechaRange(
            @Param("busId") Long busId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);
    
    @Query("SELECT d FROM DiaParadaBus d WHERE d.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY d.fecha, d.bus.id")
    List<DiaParadaBus> findByFechaRange(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);
}
