package com.andinobus.backendsmartcode.admin.domain.repositories;

import com.andinobus.backendsmartcode.admin.domain.entities.ParadaFrecuencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParadaFrecuenciaRepository extends JpaRepository<ParadaFrecuencia, Long> {

    List<ParadaFrecuencia> findByFrecuenciaViajeIdOrderByOrdenAsc(Long frecuenciaViajeId);

    void deleteByFrecuenciaViajeId(Long frecuenciaViajeId);
}
