package com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories;

import com.andinobus.backendsmartcode.cooperativa.domain.entities.FrecuenciaConfigCooperativa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FrecuenciaConfigCooperativaRepository extends JpaRepository<FrecuenciaConfigCooperativa, Long> {

    Optional<FrecuenciaConfigCooperativa> findByCooperativaId(Long cooperativaId);
}
