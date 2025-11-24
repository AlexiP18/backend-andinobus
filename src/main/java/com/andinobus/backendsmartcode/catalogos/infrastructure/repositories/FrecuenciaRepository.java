package com.andinobus.backendsmartcode.catalogos.infrastructure.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Frecuencia;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("dev")
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
}
