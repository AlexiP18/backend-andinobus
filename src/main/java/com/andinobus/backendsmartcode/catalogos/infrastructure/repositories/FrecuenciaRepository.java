package com.andinobus.backendsmartcode.catalogos.infrastructure.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Frecuencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FrecuenciaRepository extends JpaRepository<Frecuencia, Long> {
    Page<Frecuencia> findByCooperativa_IdAndActivaTrue(Long cooperativaId, Pageable pageable);
    Page<Frecuencia> findByCooperativa_IdAndActivaTrueAndOrigenContainingIgnoreCaseOrCooperativa_IdAndActivaTrueAndDestinoContainingIgnoreCase(Long cooperativaId1, String origen,
                                                                                                                                                 Long cooperativaId2, String destino,
                                                                                                                                                 Pageable pageable);

    // Necesario para RutasService
    List<Frecuencia> findByOrigenAndDestinoAndActivaTrue(String origen, String destino);
    List<Frecuencia> findByOrigenAndActivaTrue(String origen);
    List<Frecuencia> findByDestinoAndActivaTrue(String destino);
    
    // Usado por ViajeService
    List<Frecuencia> findByActivaTrue();

    // Métodos adicionales (del domain repository)
    @Query("SELECT f FROM Frecuencia f WHERE f.cooperativa.id = :cooperativaId AND f.activa = true")
    List<Frecuencia> findByCooperativaIdAndActivaTrue(@Param("cooperativaId") Long cooperativaId);

    @Query("SELECT f FROM Frecuencia f WHERE f.origen = :origen AND f.destino = :destino " +
           "AND f.cooperativa.id = :cooperativaId AND f.activa = true")
    List<Frecuencia> findByOrigenAndDestinoAndCooperativaId(
            @Param("origen") String origen,
            @Param("destino") String destino,
            @Param("cooperativaId") Long cooperativaId
    );
}
