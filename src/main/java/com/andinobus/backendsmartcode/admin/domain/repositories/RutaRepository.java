package com.andinobus.backendsmartcode.admin.domain.repositories;

import com.andinobus.backendsmartcode.admin.domain.entities.Ruta;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {
    
    List<Ruta> findByActivoTrue();
    
    List<Ruta> findByAprobadaAntTrue();
    
    List<Ruta> findByActivoTrueAndAprobadaAntTrue();
    
    List<Ruta> findByActivoTrueAndTipoRuta(String tipoRuta);
    
    boolean existsByNombreAndActivoTrue(String nombre);
    
    boolean existsByTerminalOrigenIdAndTerminalDestinoId(Long terminalOrigenId, Long terminalDestinoId);
    
    Optional<Ruta> findByNombre(String nombre);
    
    /**
     * Busca una ruta por terminal origen y destino
     */
    Optional<Ruta> findByTerminalOrigenAndTerminalDestino(Terminal terminalOrigen, Terminal terminalDestino);
    
    /**
     * Busca una ruta por IDs de terminal origen y destino
     */
    Optional<Ruta> findByTerminalOrigenIdAndTerminalDestinoId(Long terminalOrigenId, Long terminalDestinoId);
}
