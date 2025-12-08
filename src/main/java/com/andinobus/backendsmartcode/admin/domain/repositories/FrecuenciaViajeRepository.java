package com.andinobus.backendsmartcode.admin.domain.repositories;

import com.andinobus.backendsmartcode.admin.domain.entities.FrecuenciaViaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FrecuenciaViajeRepository extends JpaRepository<FrecuenciaViaje, Long> {

    List<FrecuenciaViaje> findByBusIdAndActivoTrue(Long busId);

    List<FrecuenciaViaje> findByRutaIdAndActivoTrue(Long rutaId);

    List<FrecuenciaViaje> findByBusCooperativaIdAndActivoTrue(Long cooperativaId);

    @Query("SELECT f FROM FrecuenciaViaje f WHERE f.bus.id = :busId AND f.activo = true ORDER BY f.horaSalida ASC")
    List<FrecuenciaViaje> findByBusIdOrderByHoraSalida(@Param("busId") Long busId);

    @Query("SELECT f FROM FrecuenciaViaje f WHERE f.bus.cooperativa.id = :cooperativaId AND f.activo = true ORDER BY f.horaSalida ASC")
    List<FrecuenciaViaje> findByCooperativaIdOrderByHoraSalida(@Param("cooperativaId") Long cooperativaId);

    boolean existsByBusIdAndRutaIdAndHoraSalidaAndActivoTrue(Long busId, Long rutaId, java.time.LocalTime horaSalida);

    @Query("SELECT f FROM FrecuenciaViaje f WHERE f.bus.id IN :busIds AND f.activo = true ORDER BY f.horaSalida ASC")
    List<FrecuenciaViaje> findByBusIdInAndActivoTrue(@Param("busIds") List<Long> busIds);

    @Query("SELECT f FROM FrecuenciaViaje f WHERE f.chofer.id = :choferId AND f.activo = true ORDER BY f.horaSalida ASC")
    List<FrecuenciaViaje> findByChoferIdAndActivoTrue(@Param("choferId") Long choferId);
}
