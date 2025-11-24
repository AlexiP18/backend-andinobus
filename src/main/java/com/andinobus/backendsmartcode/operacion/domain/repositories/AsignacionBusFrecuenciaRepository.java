package com.andinobus.backendsmartcode.operacion.domain.repositories;

import com.andinobus.backendsmartcode.operacion.domain.entities.AsignacionBusFrecuencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionBusFrecuenciaRepository extends JpaRepository<AsignacionBusFrecuencia, Long> {
    
    List<AsignacionBusFrecuencia> findByBusId(Long busId);
    
    List<AsignacionBusFrecuencia> findByFrecuenciaId(Long frecuenciaId);
    
    List<AsignacionBusFrecuencia> findByEstado(String estado);
    
    @Query("SELECT a FROM AsignacionBusFrecuencia a WHERE a.bus.id = :busId " +
           "AND a.estado = 'ACTIVA' " +
           "AND a.fechaInicio <= :fecha " +
           "AND (a.fechaFin IS NULL OR a.fechaFin >= :fecha)")
    Optional<AsignacionBusFrecuencia> findAsignacionActivaByBusAndFecha(
            @Param("busId") Long busId, 
            @Param("fecha") LocalDate fecha);
    
    @Query("SELECT a FROM AsignacionBusFrecuencia a WHERE a.frecuencia.id = :frecuenciaId " +
           "AND a.estado = 'ACTIVA' " +
           "AND a.fechaInicio <= :fecha " +
           "AND (a.fechaFin IS NULL OR a.fechaFin >= :fecha)")
    List<AsignacionBusFrecuencia> findAsignacionesActivasByFrecuenciaAndFecha(
            @Param("frecuenciaId") Long frecuenciaId, 
            @Param("fecha") LocalDate fecha);
}
