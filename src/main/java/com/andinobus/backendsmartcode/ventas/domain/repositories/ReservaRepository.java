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
}
