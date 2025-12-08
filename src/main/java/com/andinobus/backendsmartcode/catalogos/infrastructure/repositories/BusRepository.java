package com.andinobus.backendsmartcode.catalogos.infrastructure.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {
    Page<Bus> findByCooperativa_IdAndActivoTrue(Long cooperativaId, Pageable pageable);
    
    java.util.List<Bus> findByActivoTrue();
    
    java.util.List<Bus> findByCooperativaId(Long cooperativaId);
    
    java.util.List<Bus> findByCooperativaIdAndActivoTrue(Long cooperativaId);
    
    java.util.List<Bus> findByEstado(String estado);
    
    int countByCooperativaId(Long cooperativaId);
    
    int countByCooperativaIdAndActivoTrue(Long cooperativaId);
    
    java.util.Optional<Bus> findByPlaca(String placa);
}