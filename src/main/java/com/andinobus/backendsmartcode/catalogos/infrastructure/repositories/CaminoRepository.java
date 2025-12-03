package com.andinobus.backendsmartcode.catalogos.infrastructure.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.Camino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaminoRepository extends JpaRepository<Camino, Long> {

    List<Camino> findByRutaIdAndActivoTrue(Long rutaId);

    @Query("SELECT c FROM Camino c WHERE c.ruta.id = :rutaId AND c.activo = true ORDER BY c.tipo, c.nombre")
    List<Camino> findActiveByRutaIdOrdered(@Param("rutaId") Long rutaId);

    boolean existsByRutaIdAndNombre(Long rutaId, String nombre);
}
