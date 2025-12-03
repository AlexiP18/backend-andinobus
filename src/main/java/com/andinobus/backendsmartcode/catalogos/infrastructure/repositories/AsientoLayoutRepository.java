package com.andinobus.backendsmartcode.catalogos.infrastructure.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.entities.AsientoLayout;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("dev")
public interface AsientoLayoutRepository extends JpaRepository<AsientoLayout, Long> {

    /**
     * Encuentra todos los asientos de un bus ordenados por número
     */
    List<AsientoLayout> findByBusIdOrderByNumeroAsientoAsc(Long busId);

    /**
     * Encuentra todos los asientos de un bus ordenados por fila y columna
     */
    List<AsientoLayout> findByBusIdOrderByFilaAscColumnaAsc(Long busId);

    /**
     * Encuentra un asiento específico por bus y número de asiento
     */
    Optional<AsientoLayout> findByBusIdAndNumeroAsiento(Long busId, Integer numeroAsiento);

    /**
     * Encuentra un asiento por bus, fila y columna
     */
    Optional<AsientoLayout> findByBusIdAndFilaAndColumna(Long busId, Integer fila, Integer columna);

    /**
     * Cuenta cuántos asientos tiene un bus
     */
    @Query("SELECT COUNT(a) FROM AsientoLayout a WHERE a.bus.id = :busId")
    Long countByBusId(@Param("busId") Long busId);

    /**
     * Cuenta cuántos asientos habilitados tiene un bus
     */
    @Query("SELECT COUNT(a) FROM AsientoLayout a WHERE a.bus.id = :busId AND a.habilitado = true")
    Long countByBusIdAndHabilitadoTrue(@Param("busId") Long busId);

    /**
     * Elimina todos los asientos de un bus
     */
    @Modifying
    @Transactional
    void deleteByBusId(Long busId);

    /**
     * Elimina los asientos de un piso específico de un bus
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM AsientoLayout a WHERE a.bus.id = :busId AND a.piso = :piso")
    void deleteByBusIdAndPiso(@Param("busId") Long busId, @Param("piso") Integer piso);

    /**
     * Cuenta los asientos de un piso específico de un bus
     */
    @Query("SELECT COUNT(a) FROM AsientoLayout a WHERE a.bus.id = :busId AND a.piso = :piso")
    Long countByBusIdAndPiso(@Param("busId") Long busId, @Param("piso") Integer piso);

    /**
     * Encuentra los asientos habilitados de un bus
     */
    List<AsientoLayout> findByBusIdAndHabilitadoTrue(Long busId);

    /**
     * Obtiene el máximo número de asiento de un bus
     */
    @Query("SELECT MAX(a.numeroAsiento) FROM AsientoLayout a WHERE a.bus.id = :busId")
    Integer findMaxNumeroAsientoByBusId(@Param("busId") Long busId);

    /**
     * Obtiene el máximo número de asiento de un piso específico de un bus
     */
    @Query("SELECT MAX(a.numeroAsiento) FROM AsientoLayout a WHERE a.bus.id = :busId AND a.piso = :piso")
    Integer findMaxNumeroAsientoByBusIdAndPiso(@Param("busId") Long busId, @Param("piso") Integer piso);

    /**
     * Verifica si existe algún asiento con un número específico en un bus
     */
    @Query("SELECT COUNT(a) > 0 FROM AsientoLayout a WHERE a.bus.id = :busId AND a.numeroAsiento = :numero")
    boolean existsByBusIdAndNumeroAsiento(@Param("busId") Long busId, @Param("numero") Integer numero);

    /**
     * Obtiene las dimensiones del grid (máxima fila y columna)
     */
    @Query("SELECT MAX(a.fila) FROM AsientoLayout a WHERE a.bus.id = :busId")
    Integer findMaxFilaByBusId(@Param("busId") Long busId);

    @Query("SELECT MAX(a.columna) FROM AsientoLayout a WHERE a.bus.id = :busId")
    Integer findMaxColumnaByBusId(@Param("busId") Long busId);
}
