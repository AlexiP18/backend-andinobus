package com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories;

import com.andinobus.backendsmartcode.cooperativa.domain.entities.PlantillaRotacionFrecuencias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantillaRotacionFrecuenciasRepository extends JpaRepository<PlantillaRotacionFrecuencias, Long> {

    List<PlantillaRotacionFrecuencias> findByCooperativaIdAndActivaTrue(Long cooperativaId);

    List<PlantillaRotacionFrecuencias> findByCooperativaId(Long cooperativaId);

    Optional<PlantillaRotacionFrecuencias> findByCooperativaIdAndNombre(Long cooperativaId, String nombre);

    boolean existsByCooperativaIdAndNombre(Long cooperativaId, String nombre);
}
