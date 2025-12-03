package com.andinobus.backendsmartcode.admin.domain.repositories;

import com.andinobus.backendsmartcode.admin.domain.entities.CicloRotacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CicloRotacionRepository extends JpaRepository<CicloRotacion, Long> {

    List<CicloRotacion> findByCooperativaIdAndActivoTrue(Long cooperativaId);

    Optional<CicloRotacion> findByCooperativaIdAndNombreAndActivoTrue(Long cooperativaId, String nombre);

    List<CicloRotacion> findByCooperativaId(Long cooperativaId);
}
