package com.andinobus.backendsmartcode.admin.domain.repositories;

import com.andinobus.backendsmartcode.admin.domain.entities.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {
    
    List<Ruta> findByActivoTrue();
    
    List<Ruta> findByAprobadaAntTrue();
    
    List<Ruta> findByActivoTrueAndAprobadaAntTrue();
    
    boolean existsByNombreAndActivoTrue(String nombre);
}
