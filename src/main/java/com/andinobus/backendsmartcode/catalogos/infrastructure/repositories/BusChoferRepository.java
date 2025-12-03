package com.andinobus.backendsmartcode.catalogos.infrastructure.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.entities.BusChofer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusChoferRepository extends JpaRepository<BusChofer, Long> {

    /**
     * Obtener todos los choferes asignados a un bus
     */
    List<BusChofer> findByBusIdAndActivoTrueOrderByOrdenAsc(Long busId);

    /**
     * Obtener todos los buses asignados a un chofer
     */
    List<BusChofer> findByChoferIdAndActivoTrue(Long choferId);

    /**
     * Verificar si existe una asignación bus-chofer
     */
    Optional<BusChofer> findByBusIdAndChoferId(Long busId, Long choferId);

    /**
     * Contar choferes activos asignados a un bus
     */
    @Query("SELECT COUNT(bc) FROM BusChofer bc WHERE bc.bus.id = :busId AND bc.activo = true")
    long countActiveByBusId(@Param("busId") Long busId);

    /**
     * Verificar si un chofer ya es principal en algún bus
     */
    @Query("SELECT COUNT(bc) > 0 FROM BusChofer bc WHERE bc.chofer.id = :choferId AND bc.tipo = 'PRINCIPAL' AND bc.activo = true")
    boolean isChoferPrincipalInAnyBus(@Param("choferId") Long choferId);

    /**
     * Obtener el chofer principal de un bus
     */
    @Query("SELECT bc FROM BusChofer bc WHERE bc.bus.id = :busId AND bc.tipo = 'PRINCIPAL' AND bc.activo = true")
    Optional<BusChofer> findPrincipalByBusId(@Param("busId") Long busId);

    /**
     * Eliminar todas las asignaciones de un bus
     */
    void deleteByBusId(Long busId);

    /**
     * Obtener choferes de una cooperativa asignados a buses
     */
    @Query("SELECT bc FROM BusChofer bc WHERE bc.bus.cooperativa.id = :cooperativaId AND bc.activo = true ORDER BY bc.bus.id, bc.orden")
    List<BusChofer> findByCooperativaId(@Param("cooperativaId") Long cooperativaId);
}
