package com.andinobus.backendsmartcode.operacion.domain.repositories;

import com.andinobus.backendsmartcode.operacion.domain.entities.CalificacionViaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalificacionViajeRepository extends JpaRepository<CalificacionViaje, Long> {

    List<CalificacionViaje> findByViajeIdAndActivaTrue(Long viajeId);

    Optional<CalificacionViaje> findByViajeIdAndClienteEmailAndActivaTrue(Long viajeId, String clienteEmail);

    @Query("""
        SELECT c FROM CalificacionViaje c
        WHERE c.viaje.chofer.id = :choferId
        AND c.activa = true
        ORDER BY c.fechaCalificacion DESC
    """)
    List<CalificacionViaje> findByChoferIdOrderByFechaDesc(Long choferId);

    @Query("""
        SELECT AVG(c.puntuacion) FROM CalificacionViaje c
        WHERE c.viaje.chofer.id = :choferId
        AND c.activa = true
    """)
    Double findAverageRatingByChoferId(Long choferId);
}
