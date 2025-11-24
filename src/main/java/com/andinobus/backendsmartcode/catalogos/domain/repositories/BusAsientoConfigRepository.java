package com.andinobus.backendsmartcode.catalogos.domain.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.entities.BusAsientoConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusAsientoConfigRepository extends JpaRepository<BusAsientoConfig, Long> {

    /**
     * Busca la configuración de asientos de un bus específico
     */
    List<BusAsientoConfig> findByBusId(Long busId);
}
