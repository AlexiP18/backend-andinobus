package com.andinobus.backendsmartcode.admin.domain.repositories;

import com.andinobus.backendsmartcode.admin.domain.entities.AsignacionRotacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignacionRotacionRepository extends JpaRepository<AsignacionRotacion, Long> {

    /**
     * Obtiene asignaciones para un día específico del ciclo
     */
    List<AsignacionRotacion> findByCicloIdAndDiaCicloOrderByOrdenAsc(Long cicloId, Integer diaCiclo);

    /**
     * Obtiene todas las asignaciones de un bus en un ciclo
     */
    List<AsignacionRotacion> findByCicloIdAndBusIdOrderByDiaCicloAscOrdenAsc(Long cicloId, Long busId);

    /**
     * Obtiene asignaciones de un ciclo
     */
    List<AsignacionRotacion> findByCicloIdOrderByDiaCicloAscOrdenAsc(Long cicloId);

    /**
     * Busca asignación específica
     */
    @Query("SELECT a FROM AsignacionRotacion a " +
           "WHERE a.ciclo.id = :cicloId " +
           "AND a.diaCiclo = :diaCiclo " +
           "AND a.bus.id = :busId " +
           "AND a.orden = :orden")
    java.util.Optional<AsignacionRotacion> findAsignacionEspecifica(
        @Param("cicloId") Long cicloId,
        @Param("diaCiclo") Integer diaCiclo,
        @Param("busId") Long busId,
        @Param("orden") Integer orden
    );

    /**
     * Elimina todas las asignaciones de un ciclo
     */
    void deleteByCicloId(Long cicloId);

    /**
     * Cuenta asignaciones por día
     */
    long countByCicloIdAndDiaCiclo(Long cicloId, Integer diaCiclo);
}
