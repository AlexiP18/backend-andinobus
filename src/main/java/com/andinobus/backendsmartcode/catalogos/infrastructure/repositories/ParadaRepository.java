package com.andinobus.backendsmartcode.catalogos.infrastructure.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Parada;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParadaRepository extends JpaRepository<Parada, Long> {
    List<Parada> findByFrecuencia_Id(Long frecuenciaId, Sort sort);
    boolean existsByFrecuencia_IdAndOrden(Long frecuenciaId, Integer orden);
    Optional<Parada> findByFrecuencia_IdAndOrden(Long frecuenciaId, Integer orden);
}
