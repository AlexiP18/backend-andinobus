package com.andinobus.backendsmartcode.catalogos.infrastructure.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.entities.BusOperacionDiaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusOperacionDiariaRepository extends JpaRepository<BusOperacionDiaria, Long> {

    Optional<BusOperacionDiaria> findByBusIdAndFecha(Long busId, LocalDate fecha);

    List<BusOperacionDiaria> findByBusIdAndFechaBetween(Long busId, LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT b FROM BusOperacionDiaria b " +
           "WHERE b.bus.cooperativa.id = :cooperativaId AND b.fecha = :fecha")
    List<BusOperacionDiaria> findByCooperativaAndFecha(@Param("cooperativaId") Long cooperativaId,
                                                        @Param("fecha") LocalDate fecha);

    @Query("SELECT b FROM BusOperacionDiaria b " +
           "WHERE b.bus.id = :busId AND b.fecha = :fecha AND b.estadoBus = 'DISPONIBLE'")
    Optional<BusOperacionDiaria> findBusDisponible(@Param("busId") Long busId, @Param("fecha") LocalDate fecha);

    @Query("SELECT COALESCE(SUM(b.horasOperacion), 0) FROM BusOperacionDiaria b " +
           "WHERE b.bus.id = :busId AND b.fecha = :fecha")
    Double getHorasOperacionByBusAndFecha(@Param("busId") Long busId, @Param("fecha") LocalDate fecha);
}
