package com.andinobus.backendsmartcode.ventas.domain.repositories;

import com.andinobus.backendsmartcode.ventas.domain.entities.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    
    List<Reserva> findByClienteEmail(String clienteEmail);
    
    List<Reserva> findByViajeId(Long viajeId);
    
    @Query("SELECT r FROM Reserva r WHERE r.viaje.id = :viajeId AND r.estado IN ('PENDIENTE', 'PAGADO')")
    List<Reserva> findActiveByViajeId(@Param("viajeId") Long viajeId);
    
    @Query("SELECT r FROM Reserva r WHERE r.estado = 'PENDIENTE' AND r.expiresAt < :now")
    List<Reserva> findExpiredReservations(@Param("now") LocalDateTime now);
    
    Optional<Reserva> findByIdAndClienteEmail(Long id, String clienteEmail);
    
    /**
     * Contar reservas pendientes por cooperativa (a través del viaje y frecuencia)
     */
    @Query("SELECT COUNT(r) FROM Reserva r JOIN r.viaje v JOIN v.frecuencia f " +
           "WHERE f.cooperativa.id = :cooperativaId AND r.estado = 'PENDIENTE'")
    long countPendientesByCooperativaId(@Param("cooperativaId") Long cooperativaId);
    
    /**
     * Contar reservas por estado y cooperativa para una fecha específica
     */
    @Query("SELECT COUNT(r) FROM Reserva r JOIN r.viaje v JOIN v.frecuencia f " +
           "WHERE f.cooperativa.id = :cooperativaId AND v.fecha = :fecha AND r.estado = :estado")
    long countByCooperativaIdAndFechaAndEstado(@Param("cooperativaId") Long cooperativaId, 
                                                @Param("fecha") java.time.LocalDate fecha, 
                                                @Param("estado") String estado);
    
    /**
     * Sumar total pagado por cooperativa para una fecha específica
     * Nota: usamos el campo 'monto' de Reserva para representar el total pagado.
     */
    @Query("SELECT COALESCE(SUM(r.monto), 0) FROM Reserva r JOIN r.viaje v JOIN v.frecuencia f " +
           "WHERE f.cooperativa.id = :cooperativaId AND v.fecha = :fecha AND r.estado = 'PAGADO'")
    double sumTotalPagadoByCooperativaIdAndFecha(@Param("cooperativaId") Long cooperativaId,
                                                 @Param("fecha") java.time.LocalDate fecha);
    
    // ==================== QUERIES PARA REPORTES DE VENTAS REALES ====================
    
    /**
     * Obtener ventas totales (monto) por cooperativa en un rango de fechas
     */
    @Query("SELECT COALESCE(SUM(r.monto), 0) FROM Reserva r JOIN r.viaje v JOIN v.frecuencia f " +
           "WHERE f.cooperativa.id = :cooperativaId AND v.fecha BETWEEN :fechaInicio AND :fechaFin AND r.estado = 'PAGADO'")
    java.math.BigDecimal sumVentasByCooperativaIdAndFechaRango(
            @Param("cooperativaId") Long cooperativaId,
            @Param("fechaInicio") java.time.LocalDate fechaInicio,
            @Param("fechaFin") java.time.LocalDate fechaFin);
    
    /**
     * Contar total de transacciones (reservas pagadas) por cooperativa en un rango de fechas
     */
    @Query("SELECT COUNT(r) FROM Reserva r JOIN r.viaje v JOIN v.frecuencia f " +
           "WHERE f.cooperativa.id = :cooperativaId AND v.fecha BETWEEN :fechaInicio AND :fechaFin AND r.estado = 'PAGADO'")
    long countTransaccionesByCooperativaIdAndFechaRango(
            @Param("cooperativaId") Long cooperativaId,
            @Param("fechaInicio") java.time.LocalDate fechaInicio,
            @Param("fechaFin") java.time.LocalDate fechaFin);
    
    /**
     * Obtener reservas pagadas por cooperativa agrupadas por fecha del viaje
     */
    @Query("SELECT v.fecha as fecha, COALESCE(SUM(r.monto), 0) as monto, COUNT(r) as transacciones " +
           "FROM Reserva r JOIN r.viaje v JOIN v.frecuencia f " +
           "WHERE f.cooperativa.id = :cooperativaId AND v.fecha BETWEEN :fechaInicio AND :fechaFin AND r.estado = 'PAGADO' " +
           "GROUP BY v.fecha ORDER BY v.fecha")
    List<Object[]> findVentasPorDiaByCooperativaId(
            @Param("cooperativaId") Long cooperativaId,
            @Param("fechaInicio") java.time.LocalDate fechaInicio,
            @Param("fechaFin") java.time.LocalDate fechaFin);
    
    /**
     * Obtener ventas por ruta (usando origen/destino de la Frecuencia) para una cooperativa en un rango de fechas
     */
    @Query("SELECT f.id as frecuenciaId, f.origen as origen, f.destino as destino, " +
           "COALESCE(SUM(r.monto), 0) as ventas, COUNT(r) as boletos " +
           "FROM Reserva r JOIN r.viaje v JOIN v.frecuencia f " +
           "WHERE f.cooperativa.id = :cooperativaId AND v.fecha BETWEEN :fechaInicio AND :fechaFin AND r.estado = 'PAGADO' " +
           "GROUP BY f.id, f.origen, f.destino " +
           "ORDER BY COALESCE(SUM(r.monto), 0) DESC")
    List<Object[]> findVentasPorRutaByCooperativaId(
            @Param("cooperativaId") Long cooperativaId,
            @Param("fechaInicio") java.time.LocalDate fechaInicio,
            @Param("fechaFin") java.time.LocalDate fechaFin);
    
    // ==================== QUERIES PARA SUPERADMINISTRADOR (TODAS LAS COOPERATIVAS) ====================
    
    /**
     * Obtener ventas totales de TODAS las cooperativas en un rango de fechas
     */
    @Query("SELECT COALESCE(SUM(r.monto), 0) FROM Reserva r JOIN r.viaje v " +
           "WHERE v.fecha BETWEEN :fechaInicio AND :fechaFin AND r.estado = 'PAGADO'")
    java.math.BigDecimal sumVentasTotalesGlobal(
            @Param("fechaInicio") java.time.LocalDate fechaInicio,
            @Param("fechaFin") java.time.LocalDate fechaFin);
    
    /**
     * Contar total de transacciones globales en un rango de fechas
     */
    @Query("SELECT COUNT(r) FROM Reserva r JOIN r.viaje v " +
           "WHERE v.fecha BETWEEN :fechaInicio AND :fechaFin AND r.estado = 'PAGADO'")
    long countTransaccionesGlobal(
            @Param("fechaInicio") java.time.LocalDate fechaInicio,
            @Param("fechaFin") java.time.LocalDate fechaFin);
    
    /**
     * Obtener ventas globales por día
     */
    @Query("SELECT v.fecha as fecha, COALESCE(SUM(r.monto), 0) as monto, COUNT(r) as transacciones " +
           "FROM Reserva r JOIN r.viaje v " +
           "WHERE v.fecha BETWEEN :fechaInicio AND :fechaFin AND r.estado = 'PAGADO' " +
           "GROUP BY v.fecha ORDER BY v.fecha")
    List<Object[]> findVentasPorDiaGlobal(
            @Param("fechaInicio") java.time.LocalDate fechaInicio,
            @Param("fechaFin") java.time.LocalDate fechaFin);
    
    /**
     * Obtener ventas por cooperativa (para el superadmin)
     */
    @Query("SELECT f.cooperativa.id as cooperativaId, f.cooperativa.nombre as cooperativaNombre, " +
           "COALESCE(SUM(r.monto), 0) as ventas, COUNT(r) as transacciones " +
           "FROM Reserva r JOIN r.viaje v JOIN v.frecuencia f " +
           "WHERE v.fecha BETWEEN :fechaInicio AND :fechaFin AND r.estado = 'PAGADO' " +
           "GROUP BY f.cooperativa.id, f.cooperativa.nombre " +
           "ORDER BY COALESCE(SUM(r.monto), 0) DESC")
    List<Object[]> findVentasPorCooperativa(
            @Param("fechaInicio") java.time.LocalDate fechaInicio,
            @Param("fechaFin") java.time.LocalDate fechaFin);
}
