package com.andinobus.backendsmartcode.catalogos.infrastructure.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("dev")
public interface CooperativaRepository extends JpaRepository<Cooperativa, Long> {
    Page<Cooperativa> findByActivoTrue(Pageable pageable);
    Page<Cooperativa> findByActivoTrueAndNombreContainingIgnoreCaseOrActivoTrueAndRucContainingIgnoreCase(String nombre, String ruc, Pageable pageable);
}
