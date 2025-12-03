package com.andinobus.backendsmartcode.operacion.infrastructure.repositories;

import com.andinobus.backendsmartcode.operacion.domain.PosicionViaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PosicionViajeRepository extends JpaRepository<PosicionViaje, Long> {

    @Query("SELECT p FROM PosicionViaje p WHERE p.viaje.id = :viajeId ORDER BY p.timestamp DESC")
    List<PosicionViaje> findByViajeIdOrderByTimestampDesc(@Param("viajeId") Long viajeId);

    @Query("SELECT p FROM PosicionViaje p WHERE p.viaje.id = :viajeId AND p.timestamp >= :desde ORDER BY p.timestamp DESC")
    List<PosicionViaje> findByViajeIdAndTimestampAfter(@Param("viajeId") Long viajeId, @Param("desde") LocalDateTime desde);

    Optional<PosicionViaje> findFirstByViajeIdOrderByTimestampDesc(@Param("viajeId") Long viajeId);

    @Query("SELECT COUNT(p) FROM PosicionViaje p WHERE p.viaje.id = :viajeId")
    long countByViajeId(@Param("viajeId") Long viajeId);
}
