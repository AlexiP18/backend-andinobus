package com.andinobus.backendsmartcode.admin.domain.repositories;

import com.andinobus.backendsmartcode.admin.domain.entities.ConfiguracionGlobal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionGlobalRepository extends JpaRepository<ConfiguracionGlobal, Long> {
    
    // Siempre debería haber solo un registro de configuración
    default Optional<ConfiguracionGlobal> findCurrent() {
        return findAll().stream().findFirst();
    }
}
