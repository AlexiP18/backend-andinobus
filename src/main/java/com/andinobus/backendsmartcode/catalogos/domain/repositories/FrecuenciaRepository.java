package com.andinobus.backendsmartcode.catalogos.domain.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Frecuencia;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FrecuenciaRepository extends JpaRepository<Frecuencia, Long> {

    /**
     * Busca frecuencias activas por origen y destino
     */
    List<Frecuencia> findByOrigenAndDestinoAndActivaTrue(String origen, String destino);

    /**
     * Busca frecuencias activas de una cooperativa específica
     */
    @Query("SELECT f FROM Frecuencia f WHERE f.cooperativa.id = :cooperativaId AND f.activa = true")
    List<Frecuencia> findByCooperativaIdAndActivaTrue(@Param("cooperativaId") Long cooperativaId);

    /**
     * Busca frecuencias por origen, destino y cooperativa
     */
    @Query("SELECT f FROM Frecuencia f WHERE f.origen = :origen AND f.destino = :destino " +
           "AND f.cooperativa.id = :cooperativaId AND f.activa = true")
    List<Frecuencia> findByOrigenAndDestinoAndCooperativaId(
            @Param("origen") String origen,
            @Param("destino") String destino,
            @Param("cooperativaId") Long cooperativaId
    );

    /**
     * Busca todas las frecuencias activas
     */
    List<Frecuencia> findByActivaTrue();
}
