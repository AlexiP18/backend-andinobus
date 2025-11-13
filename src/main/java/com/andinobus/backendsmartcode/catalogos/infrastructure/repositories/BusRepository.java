package com.andinobus.backendsmartcode.catalogos.infrastructure.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("dev")
public interface BusRepository extends JpaRepository<Bus, Long> {
    Page<Bus> findByCooperativa_IdAndActivoTrue(Long cooperativaId, Pageable pageable);
}