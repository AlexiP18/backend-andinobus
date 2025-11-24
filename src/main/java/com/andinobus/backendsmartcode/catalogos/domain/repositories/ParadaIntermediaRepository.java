package com.andinobus.backendsmartcode.catalogos.domain.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.entities.ParadaIntermedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParadaIntermediaRepository extends JpaRepository<ParadaIntermedia, Long> {
    
    List<ParadaIntermedia> findByFrecuenciaIdOrderByOrdenParadaAsc(Long frecuenciaId);
    
    List<ParadaIntermedia> findByFrecuenciaIdAndActivoTrueOrderByOrdenParadaAsc(Long frecuenciaId);
}
