package com.andinobus.backendsmartcode.catalogos.infrastructure.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.Parada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParadaCaminoRepository extends JpaRepository<Parada, Long> {

    @Query("SELECT p FROM ParadaCamino p WHERE p.camino.id = :caminoId AND p.activa = true ORDER BY p.orden")
    List<Parada> findActiveByCaminoIdOrdered(@Param("caminoId") Long caminoId);

    List<Parada> findByCaminoIdOrderByOrden(Long caminoId);

    boolean existsByCaminoIdAndOrden(Long caminoId, Integer orden);

    boolean existsByCaminoIdAndNombre(Long caminoId, String nombre);
}
