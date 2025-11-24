package com.andinobus.backendsmartcode.operacion.domain.repositories;

import com.andinobus.backendsmartcode.operacion.domain.entities.ViajeAsiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ViajeAsientoRepository extends JpaRepository<ViajeAsiento, Long> {
    
    List<ViajeAsiento> findByViajeId(Long viajeId);
    
    @Query("SELECT va FROM ViajeAsiento va WHERE va.viaje.id = :viajeId AND va.numeroAsiento = :numeroAsiento")
    Optional<ViajeAsiento> findByViajeIdAndNumeroAsiento(@Param("viajeId") Long viajeId, @Param("numeroAsiento") String numeroAsiento);
    
    @Query("SELECT va FROM ViajeAsiento va WHERE va.viaje.id = :viajeId AND va.estado = 'DISPONIBLE'")
    List<ViajeAsiento> findDisponiblesByViajeId(@Param("viajeId") Long viajeId);
    
    @Query("SELECT va FROM ViajeAsiento va WHERE va.reserva.id = :reservaId")
    List<ViajeAsiento> findByReservaId(@Param("reservaId") Long reservaId);
    
    @Modifying
    @Query("UPDATE ViajeAsiento va SET va.estado = 'DISPONIBLE', va.reserva = null WHERE va.reserva.id = :reservaId")
    void liberarAsientosPorReserva(@Param("reservaId") Long reservaId);
    
    @Query("SELECT COUNT(va) FROM ViajeAsiento va WHERE va.viaje.id = :viajeId AND va.estado = 'DISPONIBLE'")
    Long countDisponiblesByViajeId(@Param("viajeId") Long viajeId);
    
    // Métodos adicionales para integración con AsientoLayout
    long countByViajeId(Long viajeId);
    
    List<ViajeAsiento> findByViajeIdOrderByNumeroAsientoAsc(Long viajeId);
    
    List<ViajeAsiento> findByViajeIdAndEstadoOrderByNumeroAsientoAsc(Long viajeId, String estado);
    
    List<ViajeAsiento> findByViajeIdAndNumeroAsientoIn(Long viajeId, List<String> numerosAsiento);
    
    long countByViajeIdAndEstado(Long viajeId, String estado);
}
