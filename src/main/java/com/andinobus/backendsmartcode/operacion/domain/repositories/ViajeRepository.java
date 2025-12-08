package com.andinobus.backendsmartcode.operacion.domain.repositories;

import com.andinobus.backendsmartcode.operacion.domain.entities.Viaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ViajeRepository extends JpaRepository<Viaje, Long> {

    /**
     * Busca viajes por fecha
     */
    List<Viaje> findByFecha(LocalDate fecha);

    /**
     * Busca viajes por frecuencia y fecha
     */
    List<Viaje> findByFrecuenciaIdAndFecha(Long frecuenciaId, LocalDate fecha);

    /**
     * Busca viajes por bus y fecha
     */
    List<Viaje> findByBusIdAndFecha(Long busId, LocalDate fecha);

    /**
     * Busca viajes activos (no cancelados) por fecha
     */
    @Query("SELECT v FROM Viaje v WHERE v.fecha = :fecha AND v.estado != 'CANCELADO'")
    List<Viaje> findActivosByFecha(@Param("fecha") LocalDate fecha);

    /**
     * Busca viajes por origen, destino y fecha a través de la frecuencia
     */
    @Query("SELECT v FROM Viaje v JOIN v.frecuencia f " +
           "WHERE f.origen = :origen AND f.destino = :destino AND v.fecha = :fecha " +
           "AND v.estado != 'CANCELADO'")
    List<Viaje> findByOrigenDestinoFecha(
            @Param("origen") String origen,
            @Param("destino") String destino,
            @Param("fecha") LocalDate fecha
    );
    
    /**
     * Contar viajes por fecha y cooperativa (a través de la frecuencia)
     */
    @Query("SELECT COUNT(v) FROM Viaje v WHERE v.fecha = :fecha AND v.frecuencia.cooperativa.id = :cooperativaId")
    long countByFechaAndFrecuenciaCooperativaId(@Param("fecha") LocalDate fecha, @Param("cooperativaId") Long cooperativaId);
    
    /**
     * Contar viajes programados para una fecha por cooperativa
     */
    @Query("SELECT COUNT(v) FROM Viaje v WHERE v.fecha = :fecha AND v.frecuencia.cooperativa.id = :cooperativaId AND v.estado = 'PROGRAMADO'")
    long countByFechaAndFrecuenciaCooperativaIdAndEstadoProgramado(@Param("fecha") LocalDate fecha, @Param("cooperativaId") Long cooperativaId);
    
    /**
     * Buscar viajes de un chofer en un rango de fechas
     */
    @Query("SELECT COUNT(v) FROM Viaje v WHERE v.chofer.id = :choferId AND v.fecha BETWEEN :fechaInicio AND :fechaFin")
    long countByChoferIdAndFechaBetween(@Param("choferId") Long choferId, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    /**
     * Buscar viaje activo de un chofer para una fecha específica
     */
    @Query("SELECT v FROM Viaje v WHERE v.chofer.id = :choferId AND v.fecha = :fecha AND v.estado IN ('PROGRAMADO', 'EN_RUTA')")
    List<Viaje> findByChoferIdAndFechaAndEstadoActivo(@Param("choferId") Long choferId, @Param("fecha") LocalDate fecha);
    
    /**
     * Buscar viaje activo de un bus para una fecha específica
     */
    @Query("SELECT v FROM Viaje v WHERE v.bus.id = :busId AND v.fecha = :fecha AND v.estado IN ('PROGRAMADO', 'EN_RUTA')")
    List<Viaje> findByBusIdAndFechaAndEstadoActivo(@Param("busId") Long busId, @Param("fecha") LocalDate fecha);
    
    /**
     * Buscar viajes disponibles de una cooperativa por fecha
     */
    @Query("SELECT v FROM Viaje v " +
           "WHERE v.frecuencia.cooperativa.id = :cooperativaId " +
           "AND v.fecha = :fecha " +
           "AND v.estado = 'PROGRAMADO' " +
           "ORDER BY v.horaSalidaProgramada")
    List<Viaje> findByCooperativaIdAndFecha(@Param("cooperativaId") Long cooperativaId, @Param("fecha") LocalDate fecha);
    
    /**
     * Buscar historial de viajes completados de un chofer
     */
    @Query("SELECT v FROM Viaje v " +
           "WHERE v.chofer.id = :choferId " +
           "AND v.estado = 'COMPLETADO' " +
           "ORDER BY v.fecha DESC, v.horaSalidaProgramada DESC")
    List<Viaje> findViajesCompletadosByChoferId(@Param("choferId") Long choferId);
    
    /**
     * Buscar viajes completados de un chofer con filtro de fecha
     */
    @Query("SELECT v FROM Viaje v " +
           "WHERE v.chofer.id = :choferId " +
           "AND v.estado = 'COMPLETADO' " +
           "AND v.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY v.fecha DESC, v.horaSalidaProgramada DESC")
    List<Viaje> findViajesCompletadosByChoferIdAndFechaBetween(
            @Param("choferId") Long choferId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    /**
     * Buscar todos los viajes de un chofer para una fecha (para cálculo de horas)
     */
    @Query("SELECT v FROM Viaje v WHERE v.chofer.id = :choferId AND v.fecha = :fecha AND v.estado != 'CANCELADO'")
    List<Viaje> findByChoferIdAndFecha(@Param("choferId") Long choferId, @Param("fecha") LocalDate fecha);
}
